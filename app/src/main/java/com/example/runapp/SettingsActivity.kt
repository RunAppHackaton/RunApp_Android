package com.example.runapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomnavigation.BottomNavigationView


class SettingsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationViewSettings)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.runningMode -> {
                    startActivity(Intent(applicationContext, RunningActivity::class.java))
                    finish()
                    true
                }
                R.id.settings -> true
                else -> false
            }
        }
        val scrollViewRoutes: ScrollView = findViewById(R.id.scrollViewRoutes)
        val scrollViewSneakers: ScrollView = findViewById(R.id.scrollViewSneakers)
        val scrollViewGoals: ScrollView = findViewById(R.id.scrollViewGoals)
        val scrollViewStatistic: ScrollView = findViewById(R.id.scrollViewStatistic)
        scrollViewRoutes.setOnClickListener {
            // You can also perform other actions here based on the click
        }
        scrollViewSneakers.setOnClickListener {
            // You can also perform other actions here based on the click
        }
        scrollViewGoals.setOnClickListener {
            // You can also perform other actions here based on the click
        }
        scrollViewStatistic.setOnClickListener {
            // You can also perform other actions here based on the click
        }
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(48.0,11.0), 15f))
        val uiSettings = googleMap.uiSettings
        uiSettings.isMyLocationButtonEnabled = false
    }
}