package com.example.runapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_running.*
import kotlinx.android.synthetic.main.run_popup_menu.*
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

    var caloriesBurnedRunning = 0.0
    var caloriesBurnedWalking = 0.0
    var sumOfCalories = 0.0

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
            postDelay()
        }
        val intent = Intent(this, LocationTrackingService::class.java)

        startForegroundService(intent)

        baseUrl = BuildConfig.BASE_URL_WORKOUT
    }

    private fun postMyData(distance: Double, time: DurationTime, points: List<RoutePointPost>) {

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

            sumOfCalories.toInt(),
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
        println(requestBody.toString())

        call.enqueue(object : Callback<CreateRunResponseBody> {
            override fun onResponse(
                call: Call<CreateRunResponseBody>,
                response: Response<CreateRunResponseBody>
            ) {
                if (response.isSuccessful) {
                    val runSession = response.body()
                    println(runSession.toString())
                    //TODO do smth with response
                } else {
                    val errorCode = response.code()
                    Log.e("RunningActivity", "Error Code: $errorCode")
                    Log.e("RunningActivity", "Error Code: $response")
                }
            }

            override fun onFailure(call: Call<CreateRunResponseBody>, t: Throwable) {
                Log.e("RunningActivity", "OnFailure: ${t.message}")
            }
        })
    }

    private fun postDelay(){
        isStarted = false

        val customMarker = BitmapDescriptorFactory.fromBitmap(createCustomMarker("finish"))
        googleMap.addMarker(MarkerOptions().position(LatLng(myLatitude, myLongitude)).icon(customMarker))

        val durTime = DurationTime(secondsPassed, false, 0, false, emptyList())
        postMyData(totalDistanceInMeters.toDouble() / 1000, durTime, listRoutePoints)

        handler.removeCallbacks(updateTextRunnable)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(applicationContext, MainActivity::class.java))
            this.finish()
        }, 3000)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            isStarted = false
            val durTime = DurationTime(secondsPassed, false, 0, false, emptyList())
            postMyData(totalDistanceInMeters.toDouble() / 1000, durTime, listRoutePoints)
            handler.removeCallbacks(updateTextRunnable)
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

    private fun createCustomMarker(startFinish: String): Bitmap {
        val diameter = 50 // Diameter of the marker circle
        val bitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        if (startFinish.equals("start")){
            paint.color = Color.GREEN
        }else if (startFinish.equals("finish")){
            paint.color = Color.BLUE
        }
        canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), (diameter / 2).toFloat(), paint)
        paint.color = Color.WHITE
        canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), (diameter / 2 - 5).toFloat(), paint)
        if (startFinish.equals("start")){
            paint.color = Color.GREEN
        }else if (startFinish.equals("finish")){
            paint.color = Color.BLUE
        }
        canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), 5f, paint)

        return bitmap
    }

    @SuppressLint("MissingPermission")
    fun drawRoute(){
        var lastUpdateTime: Long = 0

        val locationRepository = LocationRepository.getInstance()
        locationRepository.clearListeners()
        locationRepository.addListener(object : LocationRepository.LocationListener {
            override fun onLocationListUpdated(locationList: List<LatLng>) {
                if (locationList.isNotEmpty()){
                    val location = locationList[0].toLocation()
                    if (polyline.points.isEmpty()) {
                        val customMarker = BitmapDescriptorFactory.fromBitmap(createCustomMarker("start"))
                        googleMap.addMarker(MarkerOptions().position(locationList[0]).icon(customMarker))
                    }
                    val points = polyline.points
                    points.add(locationList[0])
                    polyline.points = points
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationList[0], 15f))

                    if (lastLocation != null) {
                        val currentTime = System.currentTimeMillis()
                        val timeDifference = currentTime - lastUpdateTime

                        val distance = lastLocation!!.distanceTo(location)
                        totalDistanceInMeters += distance

                        val speed = distance / (timeDifference / 1000.0f)
//                        val speed = location.speed
                        //TODO check why .speed didn't work

                        if (speed < 5){
                            pacesSeconds++
                        }
                        lastUpdateTime = currentTime

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
        if (speed !in 0f..5f) {
            caloriesBurnedRunning = (0.0175 * 8 * EXAMPLE_WEIGHT * ((secondsPassed - pacesSeconds).toDouble() / 60))
        }else if(speed > 1f){
            caloriesBurnedWalking = (0.0175 * 3.5 * EXAMPLE_WEIGHT * pacesSeconds.toDouble() / 60)
        }

        sumOfCalories = caloriesBurnedRunning + caloriesBurnedWalking

        distance.text = String.format("%.2f km", distanceInKm)

        kcal.text = String.format("%.1f", sumOfCalories)

        speedV.text = String.format("%.1f km/h", speedInKmPerHour)
    }
}

/*
TODO
achievements by sum from start
*/