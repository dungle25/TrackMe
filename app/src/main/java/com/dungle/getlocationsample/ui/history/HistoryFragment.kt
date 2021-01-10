package com.dungle.getlocationsample.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.Status
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.SessionAdapter
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.LocationHelper
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import kotlinx.android.synthetic.main.history_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.EasyPermissions

class HistoryFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel: SessionViewModel by inject()
    private lateinit var adapter: SessionAdapter
    private lateinit var locationHelper: LocationHelper
    private var currentSessionCount: Int = 0
    private val locationData: MutableList<Session> = arrayListOf()
    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val storagePerms = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    companion object {
        fun newInstance() = HistoryFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSessionAdapter()
        initLocationHelper()
        addClickEvents()
        observerDataChanged()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getSessionCount()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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
            Constant.LOCATION_REQUEST -> EasyPermissions.requestPermissions(
                this,
                "TrackMe needs your permission to access to your device location.\nPlease turn ON in your settings.",
                Constant.LOCATION_REQUEST,
                *locationPerms
            )
            Constant.BACKGROUND_REQUEST -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                EasyPermissions.requestPermissions(
                    this,
                    "TrackMe needs to run in background",
                    Constant.BACKGROUND_REQUEST,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
        }
    }

    private fun observerDataChanged() {
        viewModel.sessionCount.observe(viewLifecycleOwner, { dataResult ->
            handleDataResult(dataResult.status, {
                dataResult.data?.let {
                    if (it > 0) {
                        currentSessionCount = it
                        viewModel.getAllSessionHistory()
                    } else {
                        showNoHistoryDataMessage()
                    }
                }
            }, { showError(dataResult.message) })
        })

        viewModel.sessionHistoryData.observe(viewLifecycleOwner, { dataResult ->
            handleDataResult(dataResult.status, {
                dataResult.data?.let {
                    loadDataToList(it)
                }
            }, { showError(dataResult.message) })
        })
    }

    private fun loadDataToList(it: List<Session>) {
        locationData.addAll(it)
        adapter.notifyDataSetChanged()
    }

    // High order function
    private fun handleDataResult(
        status: Status,
        handleSuccess: () -> Unit,
        handleError: () -> Unit
    ) {
        when (status) {
            Status.SUCCESS -> {
                hideLoading()
                handleSuccess()
            }

            Status.ERROR -> {
                hideLoading()
                handleError()
            }

            else -> {
                showLoading()
            }
        }
    }

    private fun showNoHistoryDataMessage() {
        // TODO
    }

    private fun showError(message: String?) {

    }

    private fun showLoading() {

    }

    private fun hideLoading() {

    }

    private fun initSessionAdapter() {
        adapter = SessionAdapter(locationData)
        rcvLocation?.setHasFixedSize(true)
        rcvLocation?.adapter = adapter
        context?.let {
            rcvLocation?.layoutManager = LinearLayoutManager(
                it,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
    }

    private fun startTrackingLocation() {
        context?.let {
            if (Util.checkFineLocationGranted(it)
                && Util.checkAccessLocationBackgroundGranted(it)
            ) {
                navigateToRecord()
            }
        }
    }

    private fun navigateToRecord() {
        //TODO navigate to record screen
    }

    private fun initLocationHelper() {
        context?.let {
            locationHelper = LocationHelper(it)
        }
    }

    private fun verifyPermissions() {
        context?.let {
            if (!EasyPermissions.hasPermissions(
                    it,
                    *locationPerms
                )
            ) {
                EasyPermissions.requestPermissions(
                    this,
                    "TrackMe needs your permission to access to your device location.\nPlease turn ON in your settings.",
                    Constant.LOCATION_REQUEST,
                    *locationPerms
                )
            }

            if (!EasyPermissions.hasPermissions(
                    it,
                    *storagePerms
                )
            ) {
                EasyPermissions.requestPermissions(
                    this,
                    "TrackMe needs to access to media.\nPlease turn ON in your settings.",
                    Constant.LOCATION_REQUEST,
                    *storagePerms
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!EasyPermissions.hasPermissions(
                        it,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    EasyPermissions.requestPermissions(
                        this,
                        "TrackMe needs to run in background",
                        Constant.BACKGROUND_REQUEST,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )

                }
            }
        }
    }

    private fun addClickEvents() {
        btnStart?.setOnClickListener {
            updateLocations()
        }

        btnPause?.setOnClickListener {

        }
    }

    private fun updateLocations() {
        context?.let {
            if (ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                verifyPermissions()
            } else {
                startTrackingLocation()
            }
        }
    }

    @Subscribe
    fun trackNewLocation(location: Location) {
        //TODO replace event bus in service
//        locationData.add(location)
//        adapter.notifyDataSetChanged()
    }
}