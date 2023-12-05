package com.example.runapp

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class AppPreferences(context: Context) {

    private val preferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun setIsStarted(isStarted: Boolean) {
        preferences.edit().putBoolean("isStarted", isStarted).apply()
    }

    fun setVisibilityStates(isRunningBarVisible: Boolean, isStopRunButtonVisible: Boolean) {
        preferences.edit()
            .putBoolean("isRunningBarVisible", isRunningBarVisible)
            .putBoolean("isStopRunButtonVisible", isStopRunButtonVisible)
            .apply()
    }

    fun getIsStarted(): Boolean {
        return preferences.getBoolean("isStarted", false)
    }

    fun isRunningBarVisible(): Boolean {
        return preferences.getBoolean("isRunningBarVisible", false)
    }

    fun isStopRunButtonVisible(): Boolean {
        return preferences.getBoolean("isStopRunButtonVisible", false)
    }
}
