package com.dungle.getlocationsample.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dungle.getlocationsample.TrackingStatus
import com.dungle.getlocationsample.data.session.repo.SessionRepository
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.model.wrapper.DataExceptionHandler
import com.dungle.getlocationsample.model.wrapper.DataResult
import com.dungle.getlocationsample.model.wrapper.VolatileLiveData
import kotlinx.coroutines.*

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
    private var _currentInProgressSession: VolatileLiveData<DataResult<Session>> = VolatileLiveData()
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
}