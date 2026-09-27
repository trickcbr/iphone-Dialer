package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CallRecord
import com.example.model.Recording
import com.example.repository.CallHistoryRepository
import com.example.repository.RecordingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecentsFilter {
    ALL,
    MISSED
}

class RecentsViewModel(
    private val callHistoryRepository: CallHistoryRepository,
    private val recordingRepository: RecordingRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(RecentsFilter.ALL)
    val filter: StateFlow<RecentsFilter> = _filter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCall = MutableStateFlow<CallRecord?>(null)
    val selectedCall: StateFlow<CallRecord?> = _selectedCall.asStateFlow()

    private val _selectedCallRecording = MutableStateFlow<Recording?>(null)
    val selectedCallRecording: StateFlow<Recording?> = _selectedCallRecording.asStateFlow()

    val filteredCalls: StateFlow<List<CallRecord>> = combine(
        callHistoryRepository.allCalls,
        _filter,
        _searchQuery
    ) { calls, filter, query ->
        calls.filter { call ->
            val matchesFilter = when (filter) {
                RecentsFilter.ALL -> true
                RecentsFilter.MISSED -> call.callType == com.example.model.CallType.MISSED
            }
            val matchesQuery = query.isBlank() ||
                    (call.contactName?.contains(query, ignoreCase = true) == true) ||
                    call.phoneNumber.contains(query)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            callHistoryRepository.ensureDefaultHistory()
            recordingRepository.ensureDefaultRecordings()
        }
    }

    fun setFilter(filter: RecentsFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCall(call: CallRecord?) {
        _selectedCall.value = call
        if (call?.recordingId != null) {
            viewModelScope.launch {
                _selectedCallRecording.value = recordingRepository.getRecordingById(call.recordingId)
            }
        } else {
            _selectedCallRecording.value = null
        }
    }

    fun deleteCall(call: CallRecord) {
        viewModelScope.launch {
            callHistoryRepository.deleteCall(call)
            if (_selectedCall.value?.id == call.id) {
                _selectedCall.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            callHistoryRepository.clearHistory()
            _selectedCall.value = null
        }
    }
}
