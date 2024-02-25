package com.example.runapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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

    private lateinit var listViewAdapter: ExpandableListAdapter
    private lateinit var expandableListViewRoutes: ExpandableListView
    private lateinit var chapterList :  List<String>
    private lateinit var topicList : HashMap<String, List<String>>

    private var routesId: Int = 0
    private var sneakersId: Int = 0
    private var goalsId: Int = 0
    private var statisticId: Int = 0

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
        expandableListViewRoutes = findViewById(R.id.expandableListViewRoutes)

        showList()

        listViewAdapter = ExpandableListViewAdapter(this, chapterList, topicList)
        expandableListViewRoutes.setAdapter(listViewAdapter)


//        val expandableViewRoutes: ExpandableListView = findViewById(R.id.expandableListViewRoutes)
//        val expandableViewSneakers: ExpandableListView = findViewById(R.id.expandableListViewSneakers)
//        val expandableViewGoals: ExpandableListView = findViewById(R.id.expandableListViewGoals)
//        val expandableViewStatistics: ExpandableListView = findViewById(R.id.expandableListViewStatistic)
    }

    private fun showList() {
        chapterList = ArrayList()
        topicList = HashMap()

        (chapterList as ArrayList<String>).add("Chapter 1")
        (chapterList as ArrayList<String>).add("Chapter 2")
        (chapterList as ArrayList<String>).add("Chapter 3")

        val topic1: MutableList<String> = ArrayList()
        topic1.add("Topic 1")
        topic1.add("Topic 2")
        topic1.add("Topic 3")

        val topic2: MutableList<String> = ArrayList()
        topic2.add("Topic 1")
        topic2.add("Topic 2")
        topic2.add("Topic 3")

        val topic3: MutableList<String> = ArrayList()
        topic3.add("Topic 1")
        topic3.add("Topic 2")
        topic3.add("Topic 3")

        topicList[chapterList[0]] = topic1
        topicList[chapterList[1]] = topic2
        topicList[chapterList[2]] = topic3
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