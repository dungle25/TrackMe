package com.dungle.getlocationsample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.serivce.LocationUpdatesService
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var adapter: SessionAdapter
    private val locationData: MutableList<Location> = arrayListOf()
    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // A reference to the service used to get location updates.
    private var locationUpdatesService: LocationUpdatesService? = null

    // Tracks the bound state of the service.
    private var bound = false

    // Monitors the state of the connection to the service.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationUpdatesService.LocalBinder
            locationUpdatesService = binder.getService()
            bound = true
            startTrackingLocation()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationUpdatesService = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSessionAdapter()
        initLocationsStuff()
        addClickEvents()
    }

    override fun onStart() {
        super.onStart()
        verifyPermissions()
        bindLocationUpdateService()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onStop() {
        super.onStop()

        unbindLocationUpdateService()
    }

    private fun unbindLocationUpdateService() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            LOCATION_REQUEST -> EasyPermissions.requestPermissions(
                this,
                "TrackMe needs your permission to access to your device location.\nPlease turn ON in your settings.",
                LOCATION_REQUEST,
                *locationPerms
            )
            BACKGROUND_REQUEST -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "TrackMe needs to run in background",
                    BACKGROUND_REQUEST,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    private fun initSessionAdapter() {
        adapter = SessionAdapter()
        adapter.data = locationData
        rcvLocation?.adapter = adapter
        rcvLocation?.layoutManager = LinearLayoutManager(this)
    }

    private fun bindLocationUpdateService() {
        bindService(
            Intent(this, LocationUpdatesService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun startTrackingLocation() {
        if (locationUpdatesService != null
            && Util.checkFineLocationGranted(this)
            && Util.checkAccessLocationBackgroundGranted(this)
        ) {
            locationUpdatesService?.startTrackingLocation()
        }
    }

    private fun initLocationsStuff() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            interval = Constant.REQUEST_INTERVAL
            fastestInterval = Constant.FASTEST_REQUEST_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let {
                    Toast.makeText(
                        this@MainActivity,
                        "${it.latitude} - ${it.longitude}",
                        Toast.LENGTH_SHORT
                    ).show()
                    locationData.add(it)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun verifyPermissions() {
        if (!EasyPermissions.hasPermissions(
                this@MainActivity,
                *locationPerms
            )
        ) {
            EasyPermissions.requestPermissions(
                this,
                "TrackMe needs your permission to access to your device location.\nPlease turn ON in your settings.",
                LOCATION_REQUEST,
                *locationPerms
            )
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (!EasyPermissions.hasPermissions(
                    this@MainActivity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                EasyPermissions.requestPermissions(
                    this,
                    "TrackMe needs to run in background",
                    BACKGROUND_REQUEST,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )

            }
        }
    }

    private fun addClickEvents() {
        btnStart?.setOnClickListener {
            updateLocations()
        }

        btnStop?.setOnClickListener {
            stopLocationUpdates()
        }
    }

    private fun updateLocations() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            verifyPermissions()
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
//                    startLocationUpdates() //TODO
                    startTrackingLocation()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    companion object {
        private const val LOCATION_REQUEST = 123
        private const val BACKGROUND_REQUEST = 456
    }
}