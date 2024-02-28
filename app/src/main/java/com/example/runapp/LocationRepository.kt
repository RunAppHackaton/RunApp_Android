package com.example.runapp

import com.google.android.gms.maps.model.LatLng

class LocationRepository private constructor() {

    private val locationList = mutableListOf<LatLng>()
    private val listeners = mutableListOf<LocationListener>()

    interface LocationListener {
        fun onLocationListUpdated(locationList: List<LatLng>)
    }

    fun addListener(listener: LocationListener) {
        listeners.add(listener)
        listener.onLocationListUpdated(locationList)
    }

    fun clearListeners() {
        listeners.clear()
    }

    fun removeListener(listener: LocationListener) {
        listeners.remove(listener)
    }

    fun updateLocationList(newLocation: LatLng) {
        locationList.add(newLocation)
        notifyListeners()
    }

    fun removeLocation(index: Int) {
        if (index in 0 until locationList.size) {
            locationList.removeAt(index)
            notifyListeners()
        }
    }

    private fun notifyListeners() {
        listeners.forEach { it.onLocationListUpdated(locationList) }
    }

    companion object {
        private var instance: LocationRepository? = null

        fun getInstance(): LocationRepository {
            return instance ?: synchronized(this) {
                instance ?: LocationRepository().also { instance = it }
            }
        }
    }
}