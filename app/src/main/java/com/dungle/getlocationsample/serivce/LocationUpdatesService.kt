package com.dungle.getlocationsample.serivce

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.ui.MainActivity
import com.dungle.getlocationsample.util.LocationHelper
import java.util.*

/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
class LocationUpdatesService : Service() {

    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var isTracking = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    private lateinit var locationHelper: LocationHelper

    private lateinit var currentLocation: Location

    override fun onCreate() {
        Log.d(TAG, "onCreate()")

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForeGround()

        locationHelper = LocationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        if (intent?.extras?.getBoolean(EXTRA_START_SERVICE) == true) {
            startForeGround()
            startTrackingLocation()
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder {
        // BookingActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        Log.d(TAG, "onBind()")
        stopForeground(true)
        configurationChange = false
        return localBinder
    }


    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind()")

        // BookingActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange /*&& LocationUpdateUtils.isRequestingLocationUpdates(this)*/) {
            Log.d(TAG, "Start foreground service")
            startForeGround()
        }
        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        stopTrackingLocation()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun startTrackingLocation() {
        Log.d(TAG, "startTrackingLocation()")
        isTracking = true
/*
        LocationUpdateUtils.requestLocationUpdates(this, true)
*/

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, LocationUpdatesService::class.java))

        locationHelper.requestLocationUpdate(
                needRepetitiveUpdates = true,
                listener = object : LocationHelper.LocationHelperListener {
                    override fun onNewLocation(location: Location?) {
                        if (location != null) {
                            trackNewLocation(location)
                        }
                    }
                }
        )
    }

    private fun stopTrackingLocation() {
        Log.d(TAG, "stopTrackingLocation()")
        isTracking = false
        try {
            locationHelper.stopLocationUpdate()
//            LocationUpdateUtils.requestLocationUpdates(this, false)

        } catch (unlikely: SecurityException) {
//            LocationUpdateUtils.requestLocationUpdates(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    /*
    * Generates a notification
    */
    private fun generateNotification(): Notification {
        Log.d(TAG, "generateNotification()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val titleText = getString(R.string.app_name)
            val notificationChannel = NotificationChannel(
                    Constant.CHANNEL_ID_FOREGROUND_NOTIFICATION, titleText, NotificationManager.IMPORTANCE_DEFAULT)

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val launchActivityIntent = Intent(this, MainActivity::class.java)
        launchActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val extras = Bundle()
        launchActivityIntent.putExtras(extras)


        val pendingIntent = PendingIntent.getActivity(
                this, 0, launchActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationCompatBuilder =
                NotificationCompat.Builder(applicationContext, Constant.CHANNEL_ID_FOREGROUND_NOTIFICATION)

        val notificationTitle = getString(R.string.app_name)
        val notificationContext = getString(R.string.txt_app_running_in_background)

        return notificationCompatBuilder
                .setContentTitle(notificationTitle)
                .setContentText(notificationContext)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .build()
    }

    private fun startForeGround() {
        val notification = generateNotification()
        startForeground(Constant.NOTIFICATION_ID_FOREGROUND, notification)
    }

    private fun trackNewLocation(location: Location) {
        currentLocation = location

    }
    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): LocationUpdatesService = this@LocationUpdatesService
    }

    companion object {

        private const val PACKAGE_NAME = "com.deliveree.driver.service"

        const val EXTRA_START_SERVICE = "$PACKAGE_NAME.extra.START_SERVICE"

        private const val TAG = "LocationService"
    }
}
