package com.dungle.getlocationsample.ui.record

import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.model.Session
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

class RecordFragment : Fragment() {
    private val viewModel: SessionViewModel by sharedViewModel()
    private var googleMap: GoogleMap? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var isCameraMoved = false
    private var currentInProgressSession: Session? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        startTracking()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun observerDataChanged() {
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
                        startTrackingLocation()
                    }
                },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })

        viewModel.trackingState.observe(viewLifecycleOwner, { status ->
            context?.let {
                val currentSessionId = LocationUpdateUtils.getCurrentRequestingSessionId(it)
                when (status) {
                    TrackingStatus.TRACKING -> {
                        showPauseButton()
                        if (currentInProgressSession == null) {
                            currentInProgressSession = Session(currentSessionId)
                        }
                        viewModel.newSession(currentInProgressSession!!)
                    }
                    TrackingStatus.PAUSED -> {
                        showResumeAndStopButton()
                        LocationUpdateUtils.pauseTrackingService(it)
                    }
                    TrackingStatus.RESUME -> {
                        showPauseButton()
                        if (currentInProgressSession != null) {
                            // In this case, maybe there is no data in database yet, so we assume this is the 1st session
                            viewModel.newSession(currentInProgressSession!!)
                        }
                    }
                    else -> {
                        stopTracking()
                    }
                }
            }
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
                    addMarker(startLocationLatLng, currentLocationLatLng)
                } else {
                    addMarker(startLocationLatLng, null)
                }

                val latLngData = convertToLatLngList(it)
                onDrawPathWithPoint(latLngData)
                boundMapWithListLatLng(latLngData)
            }
            updateUI()
        }
    }

    private fun setLocationInfoToObject(startLocation: Location, currentLocation: Location) {
        currentInProgressSession?.distance =
            Util.calculateDistanceInKm(startLocation, currentLocation).toDouble()

        currentInProgressSession?.speeds?.add(
            Util.calculateSpeed(startLocation, currentLocation)
        )
    }

    private fun updateUI() {
        tvDistance?.text = getString(
            R.string.txt_distance,
            currentInProgressSession?.distance?.let {
                Util.round(it).toString()
            }
        )
        tvAvgSpeed?.text = getString(
            R.string.txt_speed,
            currentInProgressSession?.speeds?.let {
                Util.round(it.average()).toString()
            }
        )
        tvTime?.text = currentInProgressSession?.displayDuration
    }

    private fun convertToLatLngList(locations: MutableList<Location>): List<LatLng> {
        val latLngData: MutableList<LatLng> = arrayListOf()
        locations.forEach {
            latLngData.add(LatLng(it.latitude, it.longitude))
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
            if (LocationUpdateUtils.getCurrentRequestingSessionId(it) > -1) {
                LocationUpdateUtils.stopTrackingLocationService(it)
            }
        }
    }

    private fun startTrackingLocation() {
        activity?.let {
            if (!LocationUpdateUtils.isRequestingLocationUpdates(it)) {
                LocationUpdateUtils.startTrackingLocationService(it, currentInProgressSession!!)
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

//    private fun getValidPointBaseOnDistance(points: List<LatLng>): MutableList<LatLng> {
//        val validPoints : MutableList<LatLng> = arrayListOf()
//        for (index in points.indices) {
//            val nextIndex = index + 1
//            if (nextIndex <= points.size) {
//                val point = points[index]
//                val nextPoint = points[nextIndex]
//                val results : FloatArray = floatArrayOf()
////                distanceBetween(point.latitude, point.longitude, nextPoint.latitude, nextPoint.longitude, results)
//                distanceBetween(-14.65754,  -68.17800, 13.76664, -83.61323, results)
//            }
//        }
//        return validPoints
//    }

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
        if (isCameraMoved.not()) {
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
        }
    }

    private fun snapShotMap() {
        googleMap?.snapshot(snapShotCallback)
    }
}