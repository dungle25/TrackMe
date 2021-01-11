package com.dungle.getlocationsample.ui.record

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordFragment : Fragment() {
    private val viewModel: SessionViewModel by viewModel()

    @SuppressLint("VisibleForTests")
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var googleMap: GoogleMap? = null
    private var currentLocation: Location? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

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

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext());
        locationRequest = LocationRequest().apply {
            this.interval = Constant.REQUEST_INTERVAL
            this.fastestInterval = Constant.REQUEST_INTERVAL
            this.smallestDisplacement = Constant.SMALLEST_DISPLACEMENT
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                val newLocation = p0?.lastLocation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        getLastKnownLocation()
        startTrackingLocation()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            LocationUpdateUtils.startTrackingLocationService(it, 0)
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        getGoogleMap()
    }

    private fun getGoogleMap() {
        if (googleMap == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            mapFragment?.getMapAsync(callback)
        } else {
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            googleMap?.uiSettings?.isRotateGesturesEnabled = false
            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity,
                    R.raw.style_map
                )
            )
        }
    }

    private fun stopLocationUpdates() { //TODO call when pause or stop session
        context?.let {
            LocationUpdateUtils.stopTrackingLocationService(it)
        }
    }

    private fun pauseTracking() {
        LocationUpdateUtils.pauseTrackingLocation()
    }

    private fun startTrackingLocation() {
        activity?.let {
            LocationUpdateUtils.startTrackingLocationService(it, 0)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation() {
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                moveMapAndSetStartMarker(LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun moveMapAndSetStartMarker(location: LatLng) {
        val currentLatLng = LatLng(location.latitude, location.longitude)
        startMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(currentLatLng).title("Current")
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        Util.createStartLocationMarker(
                            requireContext()
                        )
                    )
                )
        )
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLng(
                currentLatLng
            )
        )
    }
}