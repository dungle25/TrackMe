package com.dungle.getlocationsample.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dungle.getlocationsample.R
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.data.session.repo.SessionRepository
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.model.wrapper.DataExceptionHandler
import com.dungle.getlocationsample.model.wrapper.DataResult
import com.dungle.getlocationsample.model.wrapper.VolatileLiveData
import com.dungle.getlocationsample.util.DeviceDimensionsHelper
import com.dungle.getlocationsample.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream

class SessionViewModel(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    // All session history data
    private var _sessionHistoryData: MutableLiveData<DataResult<List<Session>>> = MutableLiveData()
    val sessionHistoryData: LiveData<DataResult<List<Session>>>
        get() = _sessionHistoryData

    // Identify local database saving state
    private var _databaseSaveSessionState: MutableLiveData<DataResult<Boolean>> = MutableLiveData()
    val databaseSaveSessionState: LiveData<DataResult<Boolean>>
        get() = _databaseSaveSessionState

    // Current tracking Session
    private var _currentInProgressSession: VolatileLiveData<DataResult<Session>> =
        VolatileLiveData()
    val currentInProgressSession: LiveData<DataResult<Session>>
        get() = _currentInProgressSession

    // Request update session list
    private var _isNeedReloadSessionList: MutableLiveData<Boolean> = MutableLiveData()
    val isNeedReloadSessionList: LiveData<Boolean>
        get() = _isNeedReloadSessionList

    // Session tracking statuses
    private var _trackingState: MutableLiveData<TrackingStatus> = MutableLiveData()
    val trackingState: LiveData<TrackingStatus>
        get() = _trackingState

    var googleMap: GoogleMap? = null
    var context: Context? = null
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var isCameraMoved = false

    fun getAllSessionHistory() {
        setNeedReloadSessionList(false)
        viewModelScope.launch(Dispatchers.IO) {
            _sessionHistoryData.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val sessions = withContext(Dispatchers.IO) {
                        sessionRepository.getAllSession()
                    }
                    _sessionHistoryData.postValue(DataResult.success(sessions))
                }
            } catch (e: Exception) {
                _sessionHistoryData.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }

    fun setNeedReloadSessionList(needReload: Boolean) {
        _isNeedReloadSessionList.value = needReload
    }

    fun resetSaveSessionState() {
        _databaseSaveSessionState.value = DataResult.success(false)
    }

    fun setTrackingStatus(status: TrackingStatus) {
        _trackingState.value = status
    }

    fun saveSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            _databaseSaveSessionState.postValue(DataResult.loading(null))
            try {

                coroutineScope {
//                    val state = withContext(Dispatchers.IO) {
//                        sessionRepository.saveSession(session)
//                    }

                    val task = async {
                        sessionRepository.saveSession(session)
                    }
                    _databaseSaveSessionState.postValue(DataResult.success(task.await() > -1L))
//                    _databaseSaveSessionState.postValue(DataResult.success(state != -1L))
                }
            } catch (e: Exception) {
                _databaseSaveSessionState.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }

    fun setCurrentSession(session: Session) {
        _currentInProgressSession.postValue(DataResult.success(session))
    }

    fun getGoogleMap(mapFragment: SupportMapFragment?, context: Context) {
        if (googleMap == null) {
            mapFragment?.getMapAsync(callback)
        } else {
            googleMap?.uiSettings?.isZoomControlsEnabled = true
            googleMap?.uiSettings?.isRotateGesturesEnabled = true
            googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.style_map
                )
            )
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        googleMap.setOnCameraMoveListener {
            isCameraMoved = true
        }
        context?.let { getGoogleMap(null, it) }
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

    fun onDrawPathWithPoint(points: List<LatLng>) {
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

    fun addMarker(
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

    fun boundMapWithListLatLng(listLatLng: List<LatLng>) {
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
}