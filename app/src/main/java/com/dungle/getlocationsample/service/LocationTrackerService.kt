package com.dungle.getlocationsample.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.data.local.AppDatabase
import com.dungle.getlocationsample.ui.main.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject

class LocationTrackerService : Service() {

    private val database : AppDatabase by inject()
    private lateinit var locationRequest: LocationRequest
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.lastLocation?.let {
//                EventBus.getDefault().post(it)
                saveToLocal(it)
            }
        }

    }

    private fun saveToLocal(location: Location) {
        TODO("Not yet implemented")
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        createLocationRequest()
        fusedLocationClient = FusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prepareForegroundNotification()
        startLocationUpdates()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val titleText = getString(R.string.app_name)
            val serviceChannel = NotificationChannel(
                Constant.CHANNEL_ID_FOREGROUND_NOTIFICATION,
                titleText,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            456,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationPriority = if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            Notification.PRIORITY_HIGH
        }
        val notification = NotificationCompat
            .Builder(this, Constant.CHANNEL_ID_FOREGROUND_NOTIFICATION)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.txt_app_running_in_background))
            .setOngoing(true)
            .setPriority(notificationPriority)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .build()
        startForeground(Constant.NOTIFICATION_ID_FOREGROUND, notification)
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.interval = Constant.REQUEST_INTERVAL
        locationRequest.fastestInterval = Constant.FASTEST_REQUEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val TAG = "LocationService"
    }
}