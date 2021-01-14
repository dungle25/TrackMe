package com.dungle.getlocationsample.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.core.app.ActivityCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.service.LocationTrackerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pub.devrel.easypermissions.EasyPermissions
import java.lang.reflect.Type

class LocationUpdateUtils {
    companion object {
        private const val KEY_SESSION_STATUS = "KEY_SESSION_STATUS"
        private const val KEY_CURRENT_SESSION = "KEY_CURRENT_SESSION"
        private const val KEY_REQUESTING_LOCATION_UPDATES = "KEY_REQUESTING_LOCATION_UPDATES"

        fun isTrackingServiceRunning(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
        }

        fun setTrackingServiceRunning(context: Context, isRequestingLocationUpdates: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isRequestingLocationUpdates)
                .apply()
        }

        fun getCurrentSession(context: Context): Session? {
            val json = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_CURRENT_SESSION, "")
            val type: Type = object : TypeToken<Session>() {}.type
            return Gson().fromJson(json, type)
        }

        private fun setCurrentSession(context: Context, session: Session?) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_CURRENT_SESSION, Gson().toJson(session))
                .apply()
        }

        fun getCurrentTrackingStatus(context: Context): String? {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_SESSION_STATUS, "")
        }

        private fun setCurrentTrackingStatus(context: Context, trackingStatus: String) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_SESSION_STATUS, trackingStatus)
                .apply()
        }

        fun pauseTrackingService(context: Context?, currentInProgressSession: Session) {
            context?.let {
                setCurrentTrackingStatus(it, TrackingStatus.PAUSED)
                setCurrentSession(it, currentInProgressSession)
                setTrackingServiceRunning(context, false)
            }
        }

        fun stopTrackingLocationService(context: Context?) {
            if (context != null) {
                setCurrentSession(context, null)
                setCurrentTrackingStatus(context, TrackingStatus.STOPPED)
                setTrackingServiceRunning(context, false)
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
                setCurrentTrackingStatus(context, TrackingStatus.TRACKING)
                setTrackingServiceRunning(context, true)
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