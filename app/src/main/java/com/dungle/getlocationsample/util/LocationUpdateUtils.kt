package com.dungle.getlocationsample.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.service.LocationTrackerService
import pub.devrel.easypermissions.EasyPermissions

class LocationUpdateUtils {
    companion object {
        private const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"
        private const val REQUEST_LOCATION = 112

        fun isRequestingLocationUpdates(context: Context): Boolean {
            //TODO replace with Datastore
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
        }

        fun requestLocationUpdates(context: Context, isRequestingLocationUpdates: Boolean) {
            //TODO replace with Datastore
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates)
                .apply()
        }

        fun stopTrackingLocationService(context: Context?) {
            if (context != null) {
                requestLocationUpdates(context, false)
                context.stopService(Intent(context, LocationTrackerService::class.java))
            }
        }

        fun startTrackingLocationService(activity: Activity, sessionId : Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (EasyPermissions.hasPermissions(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    if (EasyPermissions.hasPermissions(
                            activity,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    ) {
                        checkFineLocationThenStartTracking(activity, sessionId)
                    } else {
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            REQUEST_LOCATION
                        )
                    }
                } else {
                    activity.requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_LOCATION
                    )
                }
            } else {
                checkFineLocationThenStartTracking(activity, sessionId)
            }
        }

        private fun checkFineLocationThenStartTracking(context: Activity, sessionId : Int) {
            if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestLocationUpdates(context, true)
                val intent = Intent(context, LocationTrackerService::class.java)
                intent.putExtra(Constant.CURRENT_SESSION_ID, sessionId)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION
                    )
                }
            } else {
                EasyPermissions.requestPermissions(
                    context,
                    context.getString(R.string.txt_ask_location_permission),
                    REQUEST_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }
}