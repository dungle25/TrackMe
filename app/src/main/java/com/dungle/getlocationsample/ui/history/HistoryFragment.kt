package com.dungle.getlocationsample.ui.history

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.Status
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.SessionAdapter
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import kotlinx.android.synthetic.main.history_fragment.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import pub.devrel.easypermissions.EasyPermissions

class HistoryFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel: SessionViewModel by viewModel()
    private lateinit var adapter: SessionAdapter
    private val locationData: MutableList<Session> = arrayListOf()
    private val locationPerms = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val storagePerms = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSessionAdapter()
        addClickEvents()
        observerDataChanged()
    }

    override fun onResume() {
        super.onResume()
        if (locationData.isNotEmpty()) {
            showHistoryList()
        } else {
            hideHistoryList()
        }
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
        viewModel.sessionHistoryData.observe(viewLifecycleOwner, { dataResult ->
            handleDataResult(dataResult.status, {
                onHistoryDataObserved(dataResult.data)
            }, { showError(dataResult.message) })
        })
    }

    private fun onHistoryDataObserved(data: List<Session>?) {
        if (data != null && data.isNotEmpty()) {
            loadDataToList(data)
            showHistoryList()
        } else {
            hideHistoryList()
        }
    }

    private fun hideHistoryList() {
        rcvLocation?.visibility = View.GONE
        tvNoHistory?.visibility = View.VISIBLE
    }

    private fun showHistoryList() {
        rcvLocation?.visibility = View.VISIBLE
        tvNoHistory?.visibility = View.GONE
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

    private fun showError(message: String?) {

    }

    private fun showLoading() {
        progressLoading?.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressLoading?.visibility = View.GONE
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
        btnRecord?.setOnClickListener {
            checkPermissionAndGoToRecordScreen()
        }
    }

    private fun checkPermissionAndGoToRecordScreen() {
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
                findNavController().navigate(HistoryFragmentDirections.actionHistoryFragmentToRecordFragment())
            }
        }
    }
}