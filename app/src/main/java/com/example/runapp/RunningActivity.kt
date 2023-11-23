package com.example.runapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class RunningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationViewRunning)
        bottomNavigationView.selectedItemId = R.id.home

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
                else -> false
            }
        }
    }
}