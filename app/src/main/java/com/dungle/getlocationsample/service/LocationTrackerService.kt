package com.dungle.getlocationsample.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.model.LocationData
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.main.MainActivity
import com.dungle.getlocationsample.util.StopwatchHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import org.greenrobot.eventbus.EventBus

class LocationTrackerService : Service() {

    private var session: Session? = null
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var locationRequest: LocationRequest
    private val localBinder = LocalBinder()
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult?.lastLocation?.let {
                session?.locations?.add(LocationData(it.latitude, it.longitude, it.time))
            }
        }
    }

    private var runnable = object : Runnable {
        override fun run() {
            session?.displayDuration = StopwatchHelper.toString()
            session?.duration = StopwatchHelper.currentTime - StopwatchHelper.startTime
            session?.startTime = StopwatchHelper.startTime
            session?.endTime = StopwatchHelper.currentTime
            EventBus.getDefault().post(session)
            handler.postDelayed(this, 1000)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onBind(intent: Intent): IBinder {
        return localBinder
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        createLocationRequest()
        fusedLocationClient = FusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        session = if (intent?.extras != null) {
            intent.extras!!.getParcelable(Constant.CURRENT_SESSION)
        } else {
            Session()
        }

        if (session != null && session!!.startTime > 0) {
            StopwatchHelper.startTime = session!!.startTime
            StopwatchHelper.currentTime = session!!.endTime
            StopwatchHelper.resume()
        } else {
            StopwatchHelper.start()
        }

        handler.post(runnable)

        prepareForegroundNotification()
        startLocationUpdates()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        StopwatchHelper.stop()
        session?.startTime = StopwatchHelper.startTime
        session?.endTime = StopwatchHelper.currentTime
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun pauseTracking() {
        handler.removeCallbacksAndMessages(null)
        StopwatchHelper.pause()
        session?.startTime = StopwatchHelper.startTime
        session?.endTime = StopwatchHelper.currentTime
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
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

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constant.FROM_NOTIFICATION, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 100, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationPriority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        locationRequest.smallestDisplacement = Constant.SMALLEST_DISPLACEMENT
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackerService = this@LocationTrackerService
    }
}