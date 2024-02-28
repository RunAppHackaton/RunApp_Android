package com.example.runapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.settings_activity.*


class SettingsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var listViewAdapter: ExpandableListAdapter
    private lateinit var expandableListViewRoutes: ExpandableListView
    private lateinit var chapterList :  List<String>
    private lateinit var topicList : HashMap<String, List<String>>
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNavigationView: BottomNavigationView =
            findViewById(R.id.bottomNavigationViewSettings)
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

        showList()

        listViewAdapter = ExpandableListViewAdapter(this, chapterList, topicList)
        eListView.setAdapter(listViewAdapter)
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

    private fun showList() {
        chapterList = ArrayList()
        topicList = HashMap()

        (chapterList as ArrayList<String>).add("Routes")
        (chapterList as ArrayList<String>).add("Sneakers")
        (chapterList as ArrayList<String>).add("Goals")
        (chapterList as ArrayList<String>).add("Statistic")

        val routes: MutableList<String> = ArrayList()
        routes.add("Topic 1")

        val sneakers: MutableList<String> = ArrayList()
//        sneakers.add("Topic 1")

        val goals: MutableList<String> = ArrayList()
//        goals.add("Topic 1")

        val statistic: MutableList<String> = ArrayList()
//        statistic.add("Topic 1")

        topicList[chapterList[0]] = routes
        topicList[chapterList[1]] = sneakers
        topicList[chapterList[2]] = goals
        topicList[chapterList[3]] = statistic
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