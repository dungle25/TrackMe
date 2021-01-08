package com.dungle.getlocationsample.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

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
        val UPDATE_INTERVAL_IN_MILLISECONDS: Long = TimeUnit.SECONDS.toMillis(10)

        /*
         * The fastest rate for active location updates. Updates will never be
         * more frequent than this value.
         */
        val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

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

    fun getLastKnownLocation(listener: LocationHelperListener) {
        if (hasPermission()) {
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
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null && isValidLocation(context, location)) {
                    listener.onNewLocation(location)
                } else if (location != null) {
                    listener.onDetectFakeLocation(location)
                } else {
                    listener.onNewLocation(null)
                }
            }
            task.addOnFailureListener {
                listener.onFailed(it)
            }
        } else {
            listener.onRequestPermission()
        }
    }

    private fun isValidLocation(context: Context, location: Location): Boolean {
//        return if (OutputUtil.checkMockLocation(context, location)
//                && TextUtils.isEmpty(DelivereeGlobal.getMockLocationPassCode(context))) {
//            // DLVR-10983: listen push from location service, then check if this is mock location
//            val event = BaseEvent(EventBusConstants.EVENT_FAKE_LOCATION_DETECTED, location)
//            EventBus.getDefault().post(event)
//            FirebaseAnalyticsHelper.trackingErrorEvent(FirebaseAnalyticsConstants.ERR_LOCATION_INVALID)
//            false
//        } else {
        return true
//        }
    }

    private fun hasPermission(): Boolean {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    interface LocationHelperListener {
        fun onRequestPermission() {}

        fun onFailed(e: Exception) {}

        fun onDetectFakeLocation(location: Location) {}

        fun onNewLocation(location: Location?)
    }
}