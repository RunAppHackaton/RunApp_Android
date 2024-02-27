package com.example.runapp

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
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    private lateinit var baseUrl: String
    lateinit var polyline: Polyline
    private lateinit var locationRequest : LocationRequest
    private val handler = Handler()
    private var secondsPassed = 0
    var totalDistanceInMeters: Float = 0f
    var lastLocation: Location? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private var listRoutePoints: MutableList<RoutePointPost> = ArrayList()
    var pacesSeconds: Int = 0

    val EXAMPLE_WEIGHT: Double = 80.0

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

            val durTime = DurationTime(secondsPassed, false, 0, false, emptyList())
            postMyData(totalDistanceInMeters.toDouble() / 1000, durTime, listRoutePoints, pacesSeconds)


            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
        val intent = Intent(this, LocationTrackingService::class.java)

        startForegroundService(intent)

        baseUrl = BuildConfig.BASE_URL
    }

    private fun postMyData(distance: Double, time: DurationTime, points: List<RoutePointPost>, secondsPaced: Int) {

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val requestBody = CreateRunRequestBody(
            distance_km = distance,
            duration_time = time,
//            DurationTime(
//                seconds = 3600,
//                zero = false,
//                nano = 0,
//                negative = false,
//                units = listOf(Unit(true, true, true)))

            caloriesBurned = (0.0175 * 8 * EXAMPLE_WEIGHT * (secondsPassed / 60)).toInt(),
            //TODO use real weight
            notes = "Ran in the park",
            routeId = 1,
            shoesId = 1,
            userId = 1,
            route_points = points,
//            listOf(RoutePointPost(0.0, 0.0))
            training_id_from_run_plan = 1,
            weatherConditions = "Sunny",
            //TODO add api for weather
            pace = PacePost(420, false, 0, false)
        )

        val call = service.createRunSession(requestBody)

        call.enqueue(object : Callback<CreateRunResponseBody> {
            override fun onResponse(
                call: Call<CreateRunResponseBody>,
                response: Response<CreateRunResponseBody>
            ) {
                if (response.isSuccessful) {
                    val runSession = response.body()
                    //TODO do smth with response
                } else {
                    val errorCode = response.code()
                    Log.e("RunningActivity", "Error Code: $errorCode")
                }
            }

            override fun onFailure(call: Call<CreateRunResponseBody>, t: Throwable) {
                Log.e("RunningActivity", "OnFailure: ${t.message}")
            }
        })
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
                        //TODO check why .speed didn't work

                        if (speed < 5){
                            pacesSeconds++
                        }

                        updateDistanceAndSpeed(totalDistanceInMeters, speed)
                        if (secondsPassed % 30 == 0){
                            listRoutePoints.add(RoutePointPost(location.latitude, location.longitude))
                        }
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