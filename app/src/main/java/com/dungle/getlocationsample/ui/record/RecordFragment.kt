package com.dungle.getlocationsample.ui.record

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.util.LocationUpdateUtils

class RecordFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            LocationUpdateUtils.startTrackingLocationService(it, 0)
        }
    }

    private fun stopLocationUpdates() { //TODO call when pause or stop session
        context?.let {
            LocationUpdateUtils.stopTrackingLocationService(it)
        }
    }
}