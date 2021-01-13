package com.dungle.getlocationsample.ui.history

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.ui.SessionAdapter
import com.dungle.getlocationsample.ui.viewmodel.SessionViewModel
import com.dungle.getlocationsample.util.LocationUpdateUtils
import com.dungle.getlocationsample.util.Util
import kotlinx.android.synthetic.main.history_fragment.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import pub.devrel.easypermissions.EasyPermissions

class HistoryFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private val viewModel: SessionViewModel by sharedViewModel()
    private lateinit var adapter: SessionAdapter
    private val locationData: MutableList<Session> = arrayListOf()
    private val locationPerms =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
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
        addClickEvents()
        observerDataChanged()
    }

    override fun onStart() {
        super.onStart()
        viewModel.getAllSessionHistory()
    }

    override fun onResume() {
        super.onResume()
        context?.let {
            LocationUpdateUtils.requestLocationUpdates(it, false)
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
        }
    }

    private fun observerDataChanged() {
        viewModel.sessionHistoryData.observe(viewLifecycleOwner, { dataResult ->
            Util.handleDataResult(dataResult.status,
                { onHistoryDataObserved(dataResult.data) },
                { showError(dataResult.message) },
                { hideLoading() },
                { showLoading() })
        })

        viewModel.isNeedReloadSessionList.observe(viewLifecycleOwner, { needReload ->
            if (needReload) {
                viewModel.getAllSessionHistory()
            }
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
        locationData.clear()
        locationData.addAll(it)
        adapter.notifyDataSetChanged()
    }

    private fun showError(message: String?) {
        Util.showMessage(requireContext(), message.toString())
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

    private fun addClickEvents() {
        ivRecord?.setOnClickListener {
            checkPermissionAndGoToRecordScreen()
        }
    }

    private fun checkPermissionAndGoToRecordScreen() {
        context?.let {
            val action = HistoryFragmentDirections.actionHistoryFragmentToRecordFragment()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (EasyPermissions.hasPermissions(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    if (EasyPermissions.hasPermissions(
                            it,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    ) {
                        findNavController().navigate(action)
                    } else {
                        activity?.requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            Constant.LOCATION_REQUEST
                        )
                    }
                } else {
                    activity?.requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        Constant.LOCATION_REQUEST
                    )
                }
            } else {
                findNavController().navigate(action)
            }
        }
    }
}