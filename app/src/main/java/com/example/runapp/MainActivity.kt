package com.example.runapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var baseUrlWorkout: String
    private var myLatitude: Double = 0.0
    private var myLongitude: Double = 0.0
    private var doubleBackToExitPressedOnce = false


    private var token: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //BLOCK OF CODE TO MAKE IT WORK AT EMULATOR
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationViewMain)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> true
                R.id.runningMode -> {
                    startActivity(Intent(applicationContext, RunningActivity::class.java))
                    finish()
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        val openGuild = findViewById<RelativeLayout>(R.id.openGuild)
        openGuild.setOnClickListener {
            val intent = Intent(this, GuildActivity::class.java)
            startActivity(intent)
            finish()
        }

        baseUrlWorkout = BuildConfig.BASE_URL_WORKOUT
        getMyData()

        val keycloakClient = KeycloakClient(BuildConfig.KC_BASE_URL)
        keycloakClient.getAccessToken(
            grantType = "password",
            clientId = BuildConfig.CLIENT_ID_KC,
            username = BuildConfig.USERNAME_KC,
            password = BuildConfig.PASSWORD_KC,
            callback = object : retrofit2.Callback<KeycloakToken> {
                override fun onResponse(call: Call<KeycloakToken>, response: Response<KeycloakToken>) {
                    if (response.isSuccessful) {
                        token = response.body()?.access_token
                        // Use the access token
                        println("Token ${token}")

//                        val createUserRequest = CreateUserRequest(
//                            username = "newuser1",
//                            enabled = true,
//                            email = "newuser1@example.com",
//                            credentials = listOf(
//                                UserCredential(
//                                    type = "password",
//                                    value = "password",
//                                    temporary = false
//                                )
//                            )
//                        )
//                        keycloakClient.createUser(
//                            adminToken = token!!,
//                            createUserRequest = createUserRequest,
//                            callback = object : retrofit2.Callback<Void> {
//                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                                    if (response.isSuccessful) {
//                                        println("User created successfully")
//                                    } else {
//                                        println("Failed to create user: ${response.errorBody()?.string()}")
//                                    }
//                                }
//
//                                override fun onFailure(call: Call<Void>, t: Throwable) {
//                                    println("Error calling Keycloak: ${t.message}")
//                                }
//                            }
//                        )

                    } else {
                        val errorCode = response.code()

                        Log.e("MainActivity", "Error Code: $errorCode ${response.errorBody()?.string()}")

                    }
                }

                override fun onFailure(call: Call<KeycloakToken>, t: Throwable) {
                    Log.e("MainActivity", "OnFailure: ${t.message}")

                }
            }
        )
    }

    private fun getMyData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrlWorkout)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getRunSession()

        call.enqueue(object : Callback<List<RunSession>> {
            override fun onResponse(call: Call<List<RunSession>>, response: Response<List<RunSession>>) {
                if (response.isSuccessful) {
                    val runSession = response.body()
                    println(runSession.toString())
                    println(response.errorBody())

                    //TODO do smth with response
                } else {
                    val errorCode = response.code()
                    Log.e("MainActivity", "Error Code: $errorCode ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<List<RunSession>>, t: Throwable) {
                Log.e("MainActivity", "OnFailure: ${t.message}")
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
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000

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

            fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location != null) {
                    myLatitude = location.latitude
                    myLongitude = location.longitude

                    val userLocation = LatLng(myLatitude, myLongitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= 34) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    ), PERMISSION_REQUEST_ACCESS_LOCATION
                )
            }else{
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ), PERMISSION_REQUEST_ACCESS_LOCATION
                )
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
}