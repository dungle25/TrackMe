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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Latest Session
    private var _currentInProgressSession: MutableLiveData<DataResult<Session>> = MutableLiveData()
    val currentInProgressSession: LiveData<DataResult<Session>>
        get() = _currentInProgressSession

    // Request update session list
    private var _isNeedReloadSessionList: MutableLiveData<Boolean> = MutableLiveData()
    val isNeedReloadSessionList: LiveData<Boolean>
        get() = _isNeedReloadSessionList

    // Request update session list
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
                    val state = withContext(Dispatchers.IO) {
                        sessionRepository.saveSession(session)
                    }
                    _databaseSaveSessionState.postValue(DataResult.success(state != -1L))
                }
            } catch (e: Exception) {
                _databaseSaveSessionState.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }

    fun newSession(session: Session) {
        _currentInProgressSession.postValue(DataResult.success(session))
    }
}