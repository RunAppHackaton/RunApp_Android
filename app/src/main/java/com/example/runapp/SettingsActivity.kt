package com.example.runapp

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.settings_activity.eListView


class SettingsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var listViewAdapterRoutes: ExpandableListViewAdapterRoutes
    private lateinit var listViewAdapterSneakers: ExpandableListAdapterSneakers
    private lateinit var combinedAdapter: CombinedExpandableListAdapter
    private lateinit var routeList :  List<String>
    private lateinit var routeChildList : HashMap<String, List<String>>
    private lateinit var sneakerList :  List<String>
    private lateinit var sneakerChildList :  HashMap<String, List<String>>
    private lateinit var sneakerChildImageList :  HashMap<String, List<String>>
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

        listViewAdapterRoutes = ExpandableListViewAdapterRoutes(this, routeList, routeChildList)
        listViewAdapterSneakers = ExpandableListAdapterSneakers(this, sneakerList, sneakerChildList, sneakerChildImageList)

        combinedAdapter = CombinedExpandableListAdapter(this, listViewAdapterRoutes, listViewAdapterSneakers)

        eListView.setAdapter(combinedAdapter)

        listViewAdapterRoutes.setOnChapterFooterClickListener(object : ExpandableListViewAdapterRoutes.OnChapterFooterClickListener {
            override fun onChapterFooterClicked(chapter: String) {
                Log.d("SettingsActivity", "Added route")

            }
        })
        listViewAdapterSneakers.setOnChapterFooterClickListener(object : ExpandableListAdapterSneakers.OnChapterFooterClickListener {
            override fun onChapterFooterClicked(chapter: String) {
                Log.d("SettingsActivity", "Added sneakers")

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

    private fun showList() {
        routeList = ArrayList()
        routeChildList = HashMap()
        (routeList as ArrayList<String>).add("Routes")
        val routes: MutableList<String> = ArrayList()
        routes.add("Route 1")
        routeChildList[routeList[0]] = routes

        sneakerList = ArrayList()
        sneakerChildList = HashMap()
        sneakerChildImageList = HashMap()
        (sneakerList as ArrayList<String>).add("Sneakers")
        val sneakers: MutableList<String> = ArrayList()
        val imageUris: MutableList<String> = ArrayList()
        sneakers.add("Sneakers 1")
        imageUris.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?cs=srgb&dl=pexels-anjana-c-674010.jpg&fm=jpg")
        sneakers.add("Sneakers 2")
        imageUris.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?cs=srgb&dl=pexels-anjana-c-674010.jpg&fm=jpg")
        sneakers.add("Sneakers 3")
        imageUris.add("https://images.pexels.com/photos/674010/pexels-photo-674010.jpeg?cs=srgb&dl=pexels-anjana-c-674010.jpg&fm=jpg")
        sneakerChildList[sneakerList[0]] = sneakers
        sneakerChildImageList[sneakerList[0]] = imageUris
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