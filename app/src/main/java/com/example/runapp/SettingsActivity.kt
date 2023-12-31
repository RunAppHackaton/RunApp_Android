package com.example.runapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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

    private var routesId: Int = 0
    private var sneakersId: Int = 0
    private var goalsId: Int = 0
    private var statisticId: Int = 0

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
            dynamicallyExtendScrollView(scrollViewRoutes)
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

    private fun dynamicallyExtendScrollView(scrollView: ScrollView) : Int{
        val linearLayout = LinearLayout(this)
        val id = ViewCompat.generateViewId()
        linearLayout.id = id // Use generateViewId() for API 17 and above

        // Set LinearLayout parameters
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.orientation = LinearLayout.VERTICAL

        // Add the LinearLayout to the ScrollView
        scrollView.addView(linearLayout)

        // Create a TextView for the title
        val titleTextView = TextView(this)
        titleTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleTextView.text = title
        titleTextView.textSize = 16f
        titleTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        titleTextView.setTextColor(resources.getColor(R.color.white)) // Use the appropriate color resource
        titleTextView.setPadding(0, 15, 0, 15) // Adjust padding as needed
        linearLayout.addView(titleTextView)

        // Create additional views dynamically here as needed

        // Add a separator line
        val separator = View(this)
        separator.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        separator.setBackgroundColor(resources.getColor(R.color.medium_green)) // Use the appropriate color resource
        linearLayout.addView(separator)
        return id
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