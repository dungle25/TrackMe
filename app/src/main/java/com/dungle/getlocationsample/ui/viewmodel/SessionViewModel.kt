package com.dungle.getlocationsample.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dungle.getlocationsample.data.repo.SessionRepositoryImpl
import com.dungle.getlocationsample.model.Session
import com.dungle.getlocationsample.model.wrapper.DataExceptionHandler
import com.dungle.getlocationsample.model.wrapper.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class SessionViewModel(
    private val sessionRepositoryImpl: SessionRepositoryImpl
) : ViewModel() {
    private var _sessionCount: MutableLiveData<DataResult<Int>> = MutableLiveData()
    val sessionCount: LiveData<DataResult<Int>>
        get() = _sessionCount

    private var _sessionHistoryData: MutableLiveData<DataResult<List<Session>>> = MutableLiveData()
    val sessionHistoryData: LiveData<DataResult<List<Session>>>
        get() = _sessionHistoryData

    fun getAllSessionHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionHistoryData.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val sessions = withContext(Dispatchers.IO) {
                        sessionRepositoryImpl.getAllSession()
                    }
                    _sessionHistoryData.postValue(DataResult.success(sessions))
                }
            } catch (e: Exception) {
                _sessionHistoryData.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }

    fun getSessionCount() {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionCount.postValue(DataResult.loading(null))
            try {
                coroutineScope {
                    val count = withContext(Dispatchers.IO) {
                        sessionRepositoryImpl.getSessionCount()
                    }
                    _sessionCount.postValue(DataResult.success(count))
                }
            } catch (e: Exception) {
                _sessionCount.postValue(DataExceptionHandler().handleException(e))
            }
        }
    }
}