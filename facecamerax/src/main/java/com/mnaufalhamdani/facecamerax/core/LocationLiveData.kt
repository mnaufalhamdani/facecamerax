@file:Suppress("DEPRECATION")

package com.mnaufalhamdani.facecamerax.core

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*

class LocationLiveData(context: Context, pInterval: Long = 5000, pFastestInterval: Long = 5000,
                       pPriority: Int = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
) : LiveData<LocationModel>() {

    private val locationRequest = LocationRequest.create().apply {
        interval = pInterval
        fastestInterval = pFastestInterval
        priority = pPriority
    }
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun onInactive() {
        super.onInactive()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.also {
                    setLocationData(it)
                }
            }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                setLocationData(location)
            }
        }
    }

    private fun setLocationData(location: Location) {
        val isMock = location.isFromMockProvider
        value = LocationModel(
            longitude = location.longitude,
            latitude = location.latitude,
            altitude = location.altitude,
            isMockLocation = isMock
        )
    }

    /*companion object {
        var locationRequest: LocationRequest? = null
        fun setLocationRequest(
            pInterval: Long = 5000,
            pFastestInterval: Long = 5000,
            pPriority: Int = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        ) {
            locationRequest = LocationRequest.create().apply {
                interval = pInterval
                fastestInterval = pFastestInterval
                priority = pPriority
            }
        }
    }*/
}

data class LocationModel(
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
    val isMockLocation: Boolean = false
)