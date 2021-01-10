package com.dungle.getlocationsample.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.model.BaseEvent
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus
import pub.devrel.easypermissions.EasyPermissions

class LocationHelper(private val context: Context) {

    @SuppressLint("VisibleForTests")
    private val fusedLocationProviderClient = FusedLocationProviderClient(context)
    private val locationRequest: LocationRequest
    private val settingsRequestBuilder: LocationSettingsRequest.Builder
    private val locationCallback: LocationCallback
    private var locationUpdateListener: LocationHelperListener? = null
    private var needRepetitiveUpdates = false

    init {
        locationRequest = LocationRequest().apply {
            this.interval = UPDATE_INTERVAL_IN_MILLISECONDS
            this.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            this.smallestDisplacement = SMALLEST_DISPLACEMENT
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        settingsRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                val newLocation = p0?.lastLocation
                if (newLocation != null && isValidLocation(context, newLocation)) {
                    locationUpdateListener?.onNewLocation(newLocation)
                } else if (newLocation != null) {
                    locationUpdateListener?.onDetectFakeLocation(newLocation)
                } else {
                    locationUpdateListener?.onNewLocation(null)
                }
                if (!needRepetitiveUpdates) {
                    stopLocationUpdate()
                }
            }
        }
    }

    companion object {
        /*
         * The desired interval for location updates. Inexact. Updates may be
         * more or less frequent.
         */
        const val UPDATE_INTERVAL_IN_MILLISECONDS = 1000L

        /*
         * The fastest rate for active location updates. Updates will never be
         * more frequent than this value.
         */
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 500L

        /*
         * Minimum distance to update
         */
        const val SMALLEST_DISPLACEMENT = 15.0F
    }

    fun requestLocationUpdate(needRepetitiveUpdates: Boolean, listener: LocationHelperListener) {
        this.locationUpdateListener = listener
        val settingsClient = LocationServices.getSettingsClient(context)
        val task = settingsClient.checkLocationSettings(settingsRequestBuilder.build())
        task.addOnSuccessListener {
            this.needRepetitiveUpdates = needRepetitiveUpdates
            startLocationUpdate()
        }.addOnFailureListener {
            this.needRepetitiveUpdates = needRepetitiveUpdates
            startLocationUpdate()
        }
    }

    private fun startLocationUpdate() {
        if (hasPermission()) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            locationUpdateListener?.onRequestPermission()
        }
    }

    fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun isValidLocation(context: Context, location: Location): Boolean {
        return if (Util.checkMockLocation(context, location)) {
            EventBus.getDefault().post(BaseEvent(Constant.EVENT_FAKE_LOCATION_DETECTED, location))
            false
        } else {
            return true
        }
    }

    private fun hasPermission(): Boolean {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    interface LocationHelperListener {
        fun onRequestPermission()

        fun onDetectFakeLocation(location: Location)

        fun onNewLocation(location: Location?)
    }
}