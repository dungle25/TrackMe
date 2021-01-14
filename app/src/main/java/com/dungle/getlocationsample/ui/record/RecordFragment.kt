package com.dungle.getlocationsample.ui.record

import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.ByteArrayOutputStream

class RecordFragment : Fragment() {
    private val viewModel: SessionViewModel by sharedViewModel()
    private var currentInProgressSession: Session? = null
    private var isStartNewTracking = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isStartNewTracking =
            arguments?.let { RecordFragmentArgs.fromBundle(it).isStartTracking } == true
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        context?.let {
            viewModel.getGoogleMap(mapFragment, it)
        }
        initBackPressEvent()
        initClickEvents()
        observerDataChanged()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        if (isStartNewTracking) {
            isStartNewTracking = false
            startTracking()
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun observerDataChanged() {
        viewModel.trackingState.observe(viewLifecycleOwner, { status ->
            context?.let {
                when (status) {
                    TrackingStatus.TRACKING -> {
                        showPauseButton()
                        if (currentInProgressSession == null) {
                            currentInProgressSession = Session()
                            updateUI()
                        }
                        viewModel.setCurrentSession(currentInProgressSession!!)
                    }
                    TrackingStatus.PAUSED -> {
                        showResumeAndStopButton()
                        LocationUpdateUtils.pauseTrackingService(it)
                    }
                    TrackingStatus.RESUME -> {
                        showPauseButton()
                        if (currentInProgressSession != null) {
                            viewModel.setCurrentSession(currentInProgressSession!!)
                        }
                    }
                    else -> {
                        stopTracking()
                    }
                }
            }
        })

        viewModel.databaseSaveSessionState.observe(viewLifecycleOwner, { dataResult ->
            Util.handleDataResult(
                dataResult.status,
                {
                    if (dataResult.data == true) {
                        viewModel.resetSaveSessionState()
                        viewModel.setNeedReloadSessionList(true)
                        context?.let {
                            Util.showMessage(it, ("Session is recorded!"))
                        }
                        findNavController().popBackStack()
                    }
                },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })

        viewModel.currentInProgressSession.observe(viewLifecycleOwner, { dataResult ->
            Util.handleDataResult(dataResult.status,
                {
                    dataResult.data?.let {
                        currentInProgressSession = it
                        startTrackingLocation(it)
                    }
                },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })
    }

    private fun showError(message: String?) {
        context?.let {
            Util.showMessage(it, message.toString())
        }
    }

    private fun showLoading() {
        progressLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressLoading.visibility = View.GONE
    }

    private fun initBackPressEvent() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                    stopLocationUpdates()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initClickEvents() {
        ivPause?.setOnClickListener {
            viewModel.setTrackingStatus(TrackingStatus.PAUSED)
        }

        ivResume?.setOnClickListener {
            viewModel.setTrackingStatus(TrackingStatus.RESUME)
        }

        ivStop?.setOnClickListener {
            viewModel.setTrackingStatus(TrackingStatus.STOPPED)
        }
    }

    private fun stopTracking() {
        stopLocationUpdates()
        showPauseButton()
        snapShotMap()
    }

    private fun startTracking() {
        showPauseButton()
        viewModel.setTrackingStatus(TrackingStatus.TRACKING)
    }

    private fun showPauseButton() {
        ivPause?.visibility = View.VISIBLE
        ivResume?.visibility = View.GONE
        ivStop?.visibility = View.GONE
    }

    private fun showResumeAndStopButton() {
        ivResume?.visibility = View.VISIBLE
        ivStop?.visibility = View.VISIBLE
        ivPause?.visibility = View.GONE
    }

    @Subscribe
    fun onLocationUpdate(session: Session) {
        currentInProgressSession = session
        currentInProgressSession?.locations?.let {
            if (it.size > 0) {
                val startLocation = it[0]
                val startLocationLatLng = LatLng(startLocation.latitude, startLocation.longitude)

                if (it.size > 1) {
                    val currentLocation = it[it.size - 1]
                    val currentLocationLatLng =
                        LatLng(currentLocation.latitude, currentLocation.longitude)
                    setLocationInfoToObject(startLocation, currentLocation)
                    viewModel.addMarker(startLocationLatLng, currentLocationLatLng)
                } else {
                    setLocationInfoToObject(startLocation, null)
                    viewModel.addMarker(startLocationLatLng, null)
                }

                val latLngData = convertToLatLngList(it)
                viewModel.onDrawPathWithPoint(latLngData)
                viewModel.boundMapWithListLatLng(latLngData)
            }
            updateUI()
        }
    }

    private fun setLocationInfoToObject(startLocation: Location, currentLocation: Location?) {
        if (currentLocation != null) {
            currentInProgressSession?.distance =
                Util.calculateDistanceInKm(startLocation, currentLocation).toDouble()
            currentInProgressSession?.speeds?.add(
                Util.calculateSpeed(startLocation, currentLocation)
            )
        } else {
            currentInProgressSession?.distance = 0.0
            currentInProgressSession?.speeds?.add(0.0)
        }
    }

    private fun updateUI() {
        if (currentInProgressSession != null) {
            tvDistance?.text = if (currentInProgressSession?.distance!! > 0.0) {
                getString(
                    R.string.txt_distance,
                    Util.round(currentInProgressSession?.distance!!).toString()
                )
            } else {
                getString(R.string.txt_distance, "0.0")

            }

            tvAvgSpeed?.text =
                if (currentInProgressSession?.speeds != null && currentInProgressSession?.speeds!!.isNotEmpty()) {
                    getString(
                        R.string.txt_speed,
                        Util.round(currentInProgressSession?.speeds!!.average()).toString()
                    )
                } else {
                    getString(R.string.txt_speed, "0.0")
                }

            tvTime?.text = currentInProgressSession?.displayDuration
        }
    }

    private fun convertToLatLngList(locations: MutableList<Location>): List<LatLng> {
        val latLngData: MutableList<LatLng> = arrayListOf()
        locations.forEach {
            latLngData.add(LatLng(it.latitude, it.longitude))
        }
        return latLngData
    }

    private fun stopLocationUpdates() {
        context?.let {
            if (LocationUpdateUtils.isRequestingLocationUpdates(it)) {
                currentInProgressSession = null
                LocationUpdateUtils.stopTrackingLocationService(it)
            }
        }
    }

    private fun startTrackingLocation(currentSession: Session) {
        activity?.let {
            if (!LocationUpdateUtils.isRequestingLocationUpdates(it)) {
                LocationUpdateUtils.startTrackingLocationService(it, currentSession)
            }
        }
    }

    private val snapShotCallback: SnapshotReadyCallback = object : SnapshotReadyCallback {
        var bitmap: Bitmap? = null
        override fun onSnapshotReady(snapshot: Bitmap) {
            bitmap = snapshot
            val bos = ByteArrayOutputStream()

            try {
                context?.let {
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 60, bos)
                }

            } catch (e: Exception) {
                throw e
            }

            currentInProgressSession?.mapSnapshot = bos.toByteArray()
            saveToLocal()
        }
    }

    private fun saveToLocal() {
        currentInProgressSession?.let {
            viewModel.saveSession(it)
        }
    }

    private fun snapShotMap() {
        viewModel.googleMap?.snapshot(snapShotCallback)
    }
}