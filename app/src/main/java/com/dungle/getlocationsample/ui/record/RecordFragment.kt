package com.dungle.getlocationsample.ui.record

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.data.session.work_manager.InsertSessionWorker
import com.dungle.getlocationsample.model.LocationData
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.model.wrapper.DataResult
import com.dungle.getlocationsample.service.LocationTrackerService
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.DeviceDimensionsHelper
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.ByteArrayOutputStream

open class RecordFragment : Fragment() {
    private val viewModel: SessionViewModel by sharedViewModel()
    private var googleMap: GoogleMap? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var isCameraMoved = false
    private var currentInProgressSession: Session? = null
    private var isStartNewTracking = false

    // A reference to the service used to get location updates.
    private var trackingService: LocationTrackerService? = null

    // Tracks the bound state of the service.
    private var isServiceBound = false

    // Monitors the state of the connection to the service.
    private val trackingServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationTrackerService.LocalBinder
            trackingService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            trackingService = null
            isServiceBound = false
        }
    }

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
        mapFragment?.getMapAsync(callback)
        initBackPressEvent()
        initClickEvents()
        observerDataChanged()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        bindLocationUpdateService()
        if (isStartNewTracking) {
            isStartNewTracking = false
            startTracking()
        } else {
            context?.let {
                if (LocationUpdateUtils.getCurrentTrackingStatus(it) == TrackingStatus.PAUSED) {
                    currentInProgressSession = LocationUpdateUtils.getCurrentSession(it)
                    currentInProgressSession?.let { session ->
                        onLocationUpdate(session)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        unbindLocationUpdateService()
    }

    protected open fun bindLocationUpdateService() {
        // bind Service
        context?.let {
            it.bindService(
                Intent(it, LocationTrackerService::class.java),
                trackingServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    protected open fun unbindLocationUpdateService() {
        // unbind Service
        if (isServiceBound) {
            context?.let {
                it.unbindService(trackingServiceConnection)
                isServiceBound = false
            }
        }
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
                        trackingService?.pauseTracking()
                        LocationUpdateUtils.pauseTrackingService(it, currentInProgressSession!!)
                    }
                    TrackingStatus.RESUME -> {
                        resumeTracking()
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

    private fun resumeTracking() {
        showPauseButton()
        if (currentInProgressSession != null) {
            viewModel.setCurrentSession(currentInProgressSession!!)
        }
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
                val startLocationLatLng = LatLng(startLocation.lat, startLocation.long)

                if (it.size > 1) {
                    val currentLocation = it[it.size - 1]
                    val currentLocationLatLng =
                        LatLng(currentLocation.lat, currentLocation.long)
                    setLocationInfoToObject(startLocation, currentLocation)
                    addMarker(startLocationLatLng, currentLocationLatLng)
                } else {
                    setLocationInfoToObject(startLocation, null)
                    addMarker(startLocationLatLng, null)
                }

                val latLngData = convertToLatLngList(it)
                onDrawPathWithPoint(latLngData)
                boundMapWithListLatLng(latLngData)
            }
            updateUI()
        }
    }

    private fun setLocationInfoToObject(
        startLocation: LocationData,
        currentLocation: LocationData?
    ) {
        if (currentLocation != null) {
            currentInProgressSession?.distance =
                Util.calculateDistance(
                    startLocation.lat,
                    startLocation.long,
                    currentLocation.lat,
                    currentLocation.long
                )
            currentInProgressSession?.displayAvgSpeed =
                Util.calculateSpeed(startLocation, currentLocation)
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
                if (currentInProgressSession?.displayAvgSpeed!! > 0.0) {
                    getString(
                        R.string.txt_speed,
                        Util.round(currentInProgressSession?.displayAvgSpeed!!).toString()
                    )
                } else {
                    getString(R.string.txt_speed, "0.0")
                }

            tvTime?.text = currentInProgressSession?.displayDuration
        }
    }

    private fun convertToLatLngList(locations: MutableList<LocationData>): List<LatLng> {
        val latLngData: MutableList<LatLng> = arrayListOf()
        locations.forEach {
            latLngData.add(it.toLatLng())
        }
        return latLngData
    }

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        googleMap.setOnCameraMoveListener {
            isCameraMoved = true
        }
        getGoogleMap()
    }

    private fun getGoogleMap() {
        if (googleMap == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(callback)
        } else {
            googleMap?.uiSettings?.isZoomControlsEnabled = true
            googleMap?.uiSettings?.isRotateGesturesEnabled = true
            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity,
                    R.raw.style_map
                )
            )
        }
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

    private fun getMarker(latLng: LatLng, title: String, markerLayout: Int) = googleMap?.addMarker(
        MarkerOptions()
            .position(latLng).title(title)
            .icon(BitmapDescriptorFactory.fromBitmap(context?.let {
                Util.createStartLocationMarker(
                    it, markerLayout
                )
            }))
    )

    private val snapShotCallback: SnapshotReadyCallback = object : SnapshotReadyCallback {
        var bitmap: Bitmap? = null
        override fun onSnapshotReady(snapshot: Bitmap) {
            currentInProgressSession?.locations?.let { boundMapWithListLatLng(convertToLatLngList(it)) }
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
            context?.let { context ->
//                viewModel.saveSessionWithWorker(context, it)
//                getInsertSessionWorkerState()
                viewModel.saveSession(it)
            }

        }
    }

    private fun onDrawPathWithPoint(points: List<LatLng>) {
        val polyLineOptions = PolylineOptions()
        context?.let {
            polyLineOptions.addAll(points)
                .width(
                    DeviceDimensionsHelper.convertDpToPixelFloat(
                        10f,
                        it
                    )
                )
                .color(ContextCompat.getColor(it, R.color.orange))
        }
        googleMap?.addPolyline(polyLineOptions)
    }

    private fun addMarker(
        startLocation: LatLng,
        currentLocationLatLng: LatLng?
    ) {
        if (startMarker == null) {
            startMarker = getMarker(
                startLocation,
                "Start position",
                R.layout.layout_start_marker
            )
        }

        currentLocationLatLng?.let {
            if (endMarker == null) {
                endMarker = getMarker(
                    it,
                    "Current position",
                    R.layout.layout_end_marker
                )
            } else {
                endMarker?.position = it
            }
        }
    }

    private fun boundMapWithListLatLng(listLatLng: List<LatLng>) {
//        if (isCameraMoved.not()) {
        val boundBuilder = LatLngBounds.Builder()
        for (latLng in listLatLng) {
            boundBuilder.include(latLng)
        }

        val latLngBounds = boundBuilder.build()
        googleMap?.setOnMapLoadedCallback {
            googleMap?.animateCamera(
                context?.let { DeviceDimensionsHelper.convertDpToPixel(60f, it) }?.let {
                    CameraUpdateFactory.newLatLngBounds(
                        latLngBounds,
                        it
                    )
                }, 400, null
            )
        }
//        }
    }

    private fun snapShotMap() {
        googleMap?.snapshot(snapShotCallback)
    }

    private fun getInsertSessionWorkerState() {
        context?.let {
            WorkManager.getInstance(it)
                .getWorkInfoByIdLiveData(InsertSessionWorker.workerUUID)
                .observe(viewLifecycleOwner, { workInfo ->
                    if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                        viewModel.setSaveSessionState(DataResult.success(true))
                    }
                })
        }
    }
}