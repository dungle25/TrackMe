package com.dungle.getlocationsample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    // Store latest session Id
    private var _currentSessionId: MutableLiveData<Int> = MutableLiveData()
    val currentSessionId: LiveData<Int>
        get() = _currentSessionId

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

    fun getAllSessionHistory() {
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

    fun setCurrentSessionId(id : Int) {
        _currentSessionId.value = id
    }

    fun saveSession(session: Session) {
        viewModelScope.launch(Dispatchers.IO) {
            _databaseSaveSessionState.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val state = withContext(Dispatchers.IO) {
                        sessionRepository.saveSession(0, session)
                    }
                    _databaseSaveSessionState.postValue(DataResult.success(state != -1L))
                }
            } catch (e: Exception) {
                _databaseSaveSessionState.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }

    fun getCurrentInProgressSession(sessionId : Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentInProgressSession.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val session = withContext(Dispatchers.IO) {
                        sessionRepository.getSessionById(sessionId)
                    }
                    _currentInProgressSession.postValue(DataResult.success(session))
                }
            } catch (e: Exception) {
                _currentInProgressSession.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }
}