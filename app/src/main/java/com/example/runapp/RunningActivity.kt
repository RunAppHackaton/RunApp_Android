package com.example.runapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class RunningActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var myLatitude: Double = 0.0
    var myLongitude: Double = 0.0
    private var doubleBackToExitPressedOnce = false
    private var isStarted : Boolean = false
    private lateinit var runningBar : View
    private lateinit var stopRunButton : View
    private lateinit var timerView : TextView
    lateinit var polyline: Polyline
    private lateinit var locationRequest : LocationRequest
    private val handler = Handler()
    private var secondsPassed = 0
    var totalDistanceInMeters: Float = 0f
    var lastLocation: Location? = null
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        timerView = findViewById(R.id.timerid)
        stopRunButton = findViewById(R.id.stoprun)
        runningBar = findViewById(R.id.runningbar)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!isStarted) {
            showPopupMenu()
        }
        stopRunButton.setOnClickListener{
            isStarted = false
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
        val intent = Intent(this, LocationTrackingService::class.java)

        startForegroundService(intent)
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

    @SuppressLint("MissingPermission")
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
        locationRequest.interval = 1000

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

        polyline = googleMap.addPolyline(PolylineOptions().width(10f).color(R.color.dark_green))
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener {
            true
        }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                myLatitude = location.latitude
                myLongitude = location.longitude

                val userLocation = LatLng(myLatitude, myLongitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            }
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

        bottomNavigationView = dialogView.findViewById(R.id.bottomNavigationViewRunning)

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
            runningBar.visibility = View.VISIBLE
            stopRunButton.visibility = View.VISIBLE
            dialog.dismiss()
            startTimer()
            drawRoute()
        }
        bottomNavigationView.selectedItemId = R.id.runningMode

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.runningMode -> true
                R.id.settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
    }
    private val updateTextRunnable = object : Runnable {
        override fun run() {
            val hours = TimeUnit.SECONDS.toHours(secondsPassed.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(secondsPassed.toLong()) % 60
            val seconds = secondsPassed % 60
            timerView.text = String.format("%02d : %02d : %02d", hours, minutes, seconds)

            secondsPassed++

            handler.postDelayed(this, 1000)
        }
    }
    private fun startTimer() {
        handler.post(updateTextRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTextRunnable)
    }

    fun LatLng.toLocation(): Location {
        val location = Location("LatLngProvider")
        location.latitude = this.latitude
        location.longitude = this.longitude
        return location
    }

    @SuppressLint("MissingPermission")
    fun drawRoute(){
        val locationRepository = LocationRepository.getInstance()
        locationRepository.addListener(object : LocationRepository.LocationListener {
            override fun onLocationListUpdated(locationList: List<LatLng>) {
                if (locationList.isNotEmpty()){
                    val location = locationList[0].toLocation()
                    val points = polyline.points
                    points.add(locationList[0])
                    polyline.points = points
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationList[0], 15f))

                    if (lastLocation != null) {
                        val distance = lastLocation!!.distanceTo(location)
                        totalDistanceInMeters += distance

                        val speed = location.speed

                        updateDistanceAndSpeed(totalDistanceInMeters, speed)
                    }
                    lastLocation = location
                    locationRepository.removeLocation(0)
                }
            }
        })
    }
    private fun updateDistanceAndSpeed(distanceInMeters: Float, speed: Float) {
        val distanceInKm = distanceInMeters / 1000
        val speedInKmPerHour = speed * 3.6

        val distanceTextView: TextView = findViewById(R.id.distance)
        distanceTextView.text = String.format("%.2f km", distanceInKm)

        val speedTextView: TextView = findViewById(R.id.speed)
        speedTextView.text = String.format("%.2f km/h", speedInKmPerHour)
    }
}

/*
TODO
achievements by sum from start
*/