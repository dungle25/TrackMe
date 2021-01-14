package com.dungle.getlocationsample.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.dungle.getlocationsample.Constant
import com.dungle.getlocationsample.data.session.repo.SessionRepository
import com.dungle.getlocationsample.data.session.work_manager.InsertSessionWorker
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.model.wrapper.DataExceptionHandler
import com.dungle.getlocationsample.model.wrapper.DataResult
import com.dungle.getlocationsample.model.wrapper.VolatileLiveData
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

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
    private var _trackingState: MutableLiveData<String> = MutableLiveData()
    val trackingState: LiveData<String>
        get() = _trackingState

    fun getAllSessionHistory() {
        setNeedReloadSessionList(false)
        viewModelScope.launch(Dispatchers.IO) {
            _sessionHistoryData.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val sessions = sessionRepository.getAllSession()
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

    fun setTrackingStatus(status: String) {
        _trackingState.value = status
    }

    fun saveSession(session: Session) {
//        viewModelScope.launch {
//            _databaseSaveSessionState.value = (DataResult.loading(null))
//            try {
//                val state = sessionRepository.saveSession(session)
//                _databaseSaveSessionState.value = (DataResult.success(state != -1L))
//            } catch (e: Exception) {
//                _databaseSaveSessionState.value = (DataExceptionHandler().handleException(e))
//            }
//        }
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e("TAG", "saveSession: ${throwable.message}")
        }
        val job = viewModelScope.launch(handler) {
            _databaseSaveSessionState.value = (DataResult.loading(null))
            try {
                val state = async { sessionRepository.saveSession(session) }
                _databaseSaveSessionState.value = (DataResult.success(state.await() != -1L))
            } catch (e: Exception) {
                _databaseSaveSessionState.value = (DataExceptionHandler().handleException(e))
            }
        }
    }

    fun saveSessionWithWorker(context: Context, session: Session) {
        try {
            _databaseSaveSessionState.postValue(DataResult.loading(null))
//            InsertSessionWorker.startInsertSessionWorker(context, session)

            startWorker(session, context)
        } catch (e: Exception) {
            _databaseSaveSessionState.postValue(DataExceptionHandler().handleException(e))
        }
    }

    private fun startWorker(
        session: Session,
        context: Context
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sessionJson = Gson().toJson(session)
        val data = Data.Builder()
            .putString(Constant.KEY_INSERT_SESSION, sessionJson)
            .build()

        val strike = OneTimeWorkRequest.Builder(InsertSessionWorker::class.java)
            .setInputData(data)
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.beginWith(strike).enqueue()
    }

    fun setSaveSessionState(state: DataResult<Boolean>) {
        _databaseSaveSessionState.value = state
    }

    fun setCurrentSession(session: Session) {
        _currentInProgressSession.postValue(DataResult.success(session))
    }
}