package com.dungle.getlocationsample.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.ui.history.HistoryFragmentDirections
import com.dungle.getlocationsample.util.LocationUpdateUtils


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
            if (fromNotification && LocationUpdateUtils.getCurrentTrackingStatus(this) != TrackingStatus.STOPPED ) {
                findNavController(R.id.nav_host_fragment).navigate(HistoryFragmentDirections.actionHistoryFragmentToRecordFragment())
            }
        }
    }
}