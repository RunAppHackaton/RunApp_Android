package com.example.runapp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.UiSettings
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class RunningActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var myLatitude: Double = 0.0
    private var myLongitude: Double = 0.0
    private var doubleBackToExitPressedOnce = false
    private var isStarted : Boolean = false
    private lateinit var runningBar : View
    private lateinit var stopRunButton : View
    private lateinit var timerView : TextView
    private lateinit var polyline: Polyline
    private lateinit var locationRequest : LocationRequest
    private val handler = Handler()
    private var secondsPassed = 0
    private lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        appPreferences = AppPreferences(applicationContext)
        timerView = findViewById(R.id.timerid)
        stopRunButton = findViewById(R.id.stoprun)
        runningBar = findViewById(R.id.runningbar)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        isStarted = appPreferences.getIsStarted()
        if (!isStarted) {
            showPopupMenu()
        }
        runningBar.visibility = if (appPreferences.isRunningBarVisible()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        stopRunButton.visibility = if (appPreferences.isStopRunButtonVisible()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        stopRunButton.setOnClickListener{
            isStarted = false
            appPreferences.setIsStarted(isStarted)
            appPreferences.setVisibilityStates(
                runningBar.visibility == View.GONE,
                runningBar.visibility == View.GONE
            )
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )

            if (!success) {
                Log.e("MapActivity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapActivity", "Can't find style. Error: ", e)
        }
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1500

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    p0.lastLocation?.let { location ->
                        myLatitude = location.latitude
                        myLongitude = location.longitude

                        val userLocation = LatLng(myLatitude, myLongitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
            },
            Looper.getMainLooper()
        )
        val uiSettings = googleMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = false

        polyline = googleMap.addPolyline(PolylineOptions().width(5f).color(R.color.dark_green))
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.setOnMyLocationButtonClickListener {
                true
            }

            // Get the last known location and update the UI
            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location != null) {
                    myLatitude = location.latitude
                    myLongitude = location.longitude

                    // Move the camera to the user's location
                    val userLocation = LatLng(myLatitude, myLongitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSION_REQUEST_ACCESS_LOCATION
            )
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showPopupMenu() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView: View = inflater.inflate(R.layout.run_popup_menu, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams

        dialog.setCancelable(false)

        dialog.show()

        val popupButton: Button = dialogView.findViewById(R.id.getingrs)
        popupButton.setOnClickListener {
            isStarted = true
            appPreferences.setIsStarted(isStarted)
            appPreferences.setVisibilityStates(
                runningBar.visibility == View.VISIBLE,
                stopRunButton.visibility == View.VISIBLE
            )
            runningBar.visibility = View.VISIBLE
            stopRunButton.visibility = View.VISIBLE
            dialog.dismiss()
            startTimer()
            drawRoute()
        }
        val closeButton: Button = dialogView.findViewById(R.id.closebtn)
        closeButton.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }
    private val updateTextRunnable = object : Runnable {
        override fun run() {
            val hours = TimeUnit.SECONDS.toHours(secondsPassed.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(secondsPassed.toLong()) % 60
            val seconds = secondsPassed % 60
            timerView.text = String.format("%02d : %02d : %02d", hours, minutes, seconds)

            // Increment seconds counter
            secondsPassed++

            // Schedule the runnable to run again after 1000 milliseconds (1 second)
            handler.postDelayed(this, 1000)
        }
    }
    private fun startTimer() {
        handler.post(updateTextRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        appPreferences.setIsStarted(false)
        appPreferences.setVisibilityStates(false, false)
        handler.removeCallbacks(updateTextRunnable)
    }
    private fun drawRoute(){
        appPreferences.setIsStarted(true)
        appPreferences.setVisibilityStates(true, true)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        myLatitude = location.latitude
                        myLongitude = location.longitude

                        // Update the polyline with the new location
                        val newLatLng = LatLng(myLatitude, myLongitude)
                        val points = polyline.points
                        points.add(newLatLng)
                        polyline.points = points

                        // Move the camera to the current location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
                    }
                }
            },
            Looper.getMainLooper()
        )
    }
}