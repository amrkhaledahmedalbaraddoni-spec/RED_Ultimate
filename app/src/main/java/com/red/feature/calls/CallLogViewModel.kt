package com.red.feature.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.feature.pstn.PstnCallLog
import com.red.feature.pstn.PstnDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val pstnDao: PstnDao
) : ViewModel() {

    private val _callLogs = MutableStateFlow<List<PstnCallLog>>(emptyList())
    val callLogs: StateFlow<List<PstnCallLog>> = _callLogs

    fun load() {
        viewModelScope.launch {
            pstnDao.getAllLogs().collect { logs ->
                _callLogs.value = logs
            }
        }
    }
}
