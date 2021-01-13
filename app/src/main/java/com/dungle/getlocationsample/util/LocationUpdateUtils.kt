package com.dungle.getlocationsample.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.service.LocationTrackerService
import pub.devrel.easypermissions.EasyPermissions

class LocationUpdateUtils {
    companion object {
        private const val KEY_SESSION_ID = "KEY_SESSION_ID"
        private const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"

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

        fun getCurrentRequestingSessionId(context: Context): Int {
            //TODO replace with Datastore
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(KEY_SESSION_ID, -1)
        }

        fun saveCurrentRequestingSessionId(context: Context, sessionId: Int) {
            //TODO replace with Datastore
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(KEY_SESSION_ID, sessionId)
                .apply()
        }

        fun pauseTrackingService(context: Context?) {
            context?.let {
                requestLocationUpdates(it, false)
                it.stopService(Intent(it, LocationTrackerService::class.java))
            }
        }

        fun stopTrackingLocationService(context: Context?) {
            if (context != null) {
                requestLocationUpdates(context, false)
                saveCurrentRequestingSessionId(context, -1)
                context.stopService(Intent(context, LocationTrackerService::class.java))
            }
        }

        fun startTrackingLocationService(activity: Activity, session: Session) {
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
                        checkFineLocationThenStartTracking(activity, session)
                    } else {
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            Constant.LOCATION_REQUEST
                        )
                    }
                } else {
                    activity.requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        Constant.LOCATION_REQUEST
                    )
                }
            } else {
                checkFineLocationThenStartTracking(activity, session)
            }
        }

        private fun checkFineLocationThenStartTracking(context: Activity, session : Session) {
            if (EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestLocationUpdates(context, true)
                saveCurrentRequestingSessionId(context, session.id)
                val intent = Intent(context, LocationTrackerService::class.java)
                val bundle = Bundle()
                bundle.putParcelable(Constant.CURRENT_SESSION, session)
                intent.putExtras(bundle)
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
                        Constant.LOCATION_REQUEST
                    )
                }
            } else {
                EasyPermissions.requestPermissions(
                    context,
                    context.getString(R.string.txt_ask_location_permission),
                    Constant.LOCATION_REQUEST,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }
}