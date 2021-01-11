package com.dungle.getlocationsample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dungle.getlocationsample.data.repo.SessionRepository
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
    private var _currentSessionId: MutableLiveData<Int> = MutableLiveData()
    val currentSessionId: LiveData<Int>
        get() = _currentSessionId

    private var _sessionHistoryData: MutableLiveData<DataResult<List<Session>>> = MutableLiveData()
    val sessionHistoryData: LiveData<DataResult<List<Session>>>
        get() = _sessionHistoryData

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
}