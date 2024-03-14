package com.example.runapp

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.activity_guild.backFromGuild
import kotlinx.android.synthetic.main.activity_guild.createGuildButton
import kotlinx.android.synthetic.main.activity_guild.linearLayout
import kotlinx.android.synthetic.main.activity_guild.textset
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GuildActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var baseUrl: String

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private var doubleBackToExitPressedOnce = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guild)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        baseUrl = BuildConfig.BASE_URL_GUILD

        backFromGuild.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        createGuildButton.setOnClickListener {
            showCreateGuildPopup()
            textset.visibility = View.GONE
            linearLayout.visibility = View.GONE
            backFromGuild.visibility = View.GONE
        }
    }

    private fun showCreateGuildPopup() {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView: View = inflater.inflate(R.layout.dialog_create_guild, null)

        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val layoutParams = dialog.window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams

        dialog.setCancelable(false)

        dialog.show()

        val backToList: Button = dialogView.findViewById(R.id.backToList)
        backToList.setOnClickListener {
            dialog.hide()
            textset.visibility = View.VISIBLE
            linearLayout.visibility = View.VISIBLE
            backFromGuild.visibility = View.VISIBLE
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val createButton : Button = dialogView.findViewById(R.id.createButton)
        createButton.setOnClickListener {
            val requestBody = Team(
                teamName ="Some Name",
                descriptionTeam = "Some description",
                storyId = 0,
                maximumPlayers = 10,
                adminId = "0"
            )

            val call = service.createGuild(requestBody)
            println(requestBody.toString())

            call.enqueue(object : Callback<TeamResponse> {
                override fun onResponse(
                    call: Call<TeamResponse>,
                    response: Response<TeamResponse>
                ) {
                    if (response.isSuccessful) {
                        val newGuild = response.body()
                        Log.e("GuildActivity", "Error Code: $newGuild")
                        //TODO do smth with response
                    } else {
                        val errorCode = response.code()
                        Log.e("GuildActivity", "Error Code: $errorCode")
                        Log.e("GuildActivity", "Error Code: $response")
                    }
                }

                override fun onFailure(call: Call<TeamResponse>, t: Throwable) {
                    Log.e("GuildActivity", "OnFailure: ${t.message}")
                }
            })
        }
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