package com.example.runapp

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng


class LocationTrackingService : Service() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    var myLatitude: Double = 0.0
    var myLongitude: Double = 0.0

    private val locationList = mutableListOf<LatLng>()
    private val locationRepository = LocationRepository.getInstance()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand")
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, RunningActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "LocationTracking")
            .setContentTitle("MyService")
            .setContentText("Service is running")
            .setContentIntent(pendingIntent)
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        println("onCreate")

        val notificationChannel = NotificationChannel(
            "LocationTracking",
            "Location Tracking",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
        startForeground(123314, createNotification())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    myLatitude = location.latitude
                    myLongitude = location.longitude

                    val userLocation = LatLng(myLatitude, myLongitude)
                    locationRepository.updateLocationList(userLocation)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        super.onCreate()
    }
}