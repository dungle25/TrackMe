package com.dungle.getlocationsample.ui.record

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.DeviceDimensionsHelper
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_record.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class RecordFragment : Fragment() {
    private val viewModel: SessionViewModel by sharedViewModel()

    @SuppressLint("VisibleForTests")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var googleMap: GoogleMap? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var currentLatLng: LatLng? = null
    private var isCameraMoved = false
    private var currentInProgressSession: Session? = null
    private var currentInProgressSessionId: Int = -1

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
        initLocationStuff()
        observerDataChanged()
    }

    private fun observerDataChanged() {
        viewModel.databaseSaveSessionState.observe(viewLifecycleOwner, { dataResult ->
            Util.handleDataResult(dataResult.status,
                {
                    Util.showMessage(requireContext(), ("Session is recorded!"))
                    findNavController().popBackStack()
                },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })

        viewModel.currentInProgressSession.observe(viewLifecycleOwner, { dataResult ->
            Util.handleDataResult(dataResult.status,
                { saveSession(dataResult.data) },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })

        viewModel.currentSessionId.observe(viewLifecycleOwner, { dataResult ->
            currentInProgressSessionId = dataResult
            startTrackingLocation()
            if (dataResult != -1) {
                viewModel.getCurrentInProgressSession(dataResult)
            }
        })
    }

    private fun saveSession(data: Session?) {
        data?.let {
            currentInProgressSession = it
        }
    }

    private fun showError(message: String?) {
        Util.showMessage(requireContext(), message.toString())
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
            showResumeAndStopButton()
            viewModel.getCurrentInProgressSession(currentInProgressSessionId)
            stopLocationUpdates()
        }

        ivResume?.setOnClickListener {
            showPauseButton()
            startTrackingLocation()
        }

        ivStop?.setOnClickListener {
            showPauseButton()
            currentInProgressSession?.let {
                viewModel.saveSession(it)
            }
            stopLocationUpdates()
        }
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

    private fun initLocationStuff() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest().apply {
            this.interval = Constant.REQUEST_INTERVAL
            this.fastestInterval = Constant.REQUEST_INTERVAL
            this.smallestDisplacement = Constant.SMALLEST_DISPLACEMENT
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        getLastKnownLocation()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            LocationUpdateUtils.startTrackingLocationService(it, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onLocationUpdate(session: Session) {
        currentInProgressSession = session
        Log.e("juju", "fragment: ${currentInProgressSession?.locations?.size}")
        currentInProgressSession?.locations?.let {
            val latLngData = convertToLatLngList(it)
            onDrawPathWithPoint(latLngData)
            boundMapWithListLatLng(latLngData)
        }
    }

    private fun convertToLatLngList(locations: MutableList<Location>): List<LatLng> {
        val latLngData : MutableList<LatLng> = arrayListOf()
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
            if (LocationUpdateUtils.isRequestingLocationUpdates(it)) {
                LocationUpdateUtils.stopTrackingLocationService(it)
            }
        }
    }

    private fun startTrackingLocation() {
        activity?.let {
            if (currentInProgressSession == null) {
                currentInProgressSession = Session()
            }
            LocationUpdateUtils.startTrackingLocationService(it, currentInProgressSessionId)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLatLng = LatLng(location.latitude, location.longitude)
                moveMapAndSetStartMarker(
                    LatLng(location.latitude, location.longitude)
                )
            }
        }
    }

    val testss: MutableList<LatLng> = arrayListOf()
    private fun moveMapAndSetStartMarker(location: LatLng) {
        mockLocations()
        startMarker = getMarker(location, "Start point", R.layout.layout_start_marker)
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLng(
                location
            )
        )
    }

    private fun getMarker(latLng: LatLng, title: String, markerLayout: Int) = googleMap?.addMarker(
        MarkerOptions()
            .position(latLng).title(title)
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    Util.createStartLocationMarker(
                        requireContext(), markerLayout
                    )
                )
            )
    )

    private fun mockLocations() {
        testss.add(currentLatLng!!)
        testss.add(LatLng(10.796962, 106.659363))
        testss.add(LatLng(10.796709, 106.659523))
        testss.add(LatLng(10.796524, 106.659631))
        testss.add(LatLng(10.796357, 106.659515))
        testss.add(LatLng(10.796164, 106.659513))
        testss.add(LatLng(10.796071, 106.659572))
        testss.add(LatLng(10.795794, 106.659403))
        testss.add(LatLng(10.795524, 106.659248))
        testss.add(LatLng(10.795207, 106.659081))
        testss.add(LatLng(10.794997, 106.658944))
        testss.add(LatLng(10.794712, 106.658989))
        testss.add(LatLng(10.794440, 106.659035))
        testss.add(LatLng(10.794074, 106.659069))
        testss.add(LatLng(10.793758, 106.659107))
        testss.add(LatLng(10.793467, 106.659177))
        testss.add(LatLng(10.793301, 106.659048))
        testss.add(LatLng(10.793170, 106.659201))
        testss.add(LatLng(10.793084, 106.659390))
        testss.add(LatLng(10.792430, 106.660085))
    }

    private fun onDrawPathWithPoint(points: List<LatLng>) {
        val currentLocationLatLng =
            LatLng(points[points.size - 1].latitude, points[points.size - 1].longitude)
        val polyLineOptions = PolylineOptions()
        polyLineOptions.addAll(points)
            .width(
                DeviceDimensionsHelper.convertDpToPixelFloat(
                    10f,
                    requireContext()
                )
            )
            .color(ContextCompat.getColor(requireContext(), R.color.orange))
        googleMap?.addPolyline(polyLineOptions)
        if (endMarker == null) {
            endMarker = getMarker(
                currentLocationLatLng,
                "Current position",
                R.layout.layout_end_marker
            )
        } else {
            endMarker?.position = currentLocationLatLng
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
                    CameraUpdateFactory.newLatLngBounds(
                        latLngBounds,
                        DeviceDimensionsHelper.convertDpToPixel(60f, requireContext())
                    ), 400, null
                )
            }
        }
    }
}