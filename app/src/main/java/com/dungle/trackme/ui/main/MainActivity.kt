package com.dungle.trackme.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.dungle.trackme.Constant
import com.dungle.trackme.R
import com.dungle.trackme.ui.history.HistoryFragmentDirections
import com.dungle.trackme.util.LocationUpdateUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val extras = intent.extras
        if (extras != null) {
            val fromNotification = extras.getBoolean(Constant.FROM_NOTIFICATION, false)
            val isRunning = LocationUpdateUtils.isTrackingServiceRunning(this)
            if (fromNotification && isRunning) {
                intent.removeExtra(Constant.FROM_NOTIFICATION)
                findNavController(R.id.nav_host_fragment).navigate(HistoryFragmentDirections.actionHistoryFragmentToRecordFragment())
            }
        }
    }
}