package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val chatApi: ChatApi
) : ViewModel() {

    private val _messages = MutableStateFlow<List<StoredMessageDto>>(emptyList())
    val messages: StateFlow<List<StoredMessageDto>> = _messages

    private val _users = MutableStateFlow<List<UserSearchResultDto>>(emptyList())
    val users: StateFlow<List<UserSearchResultDto>> = _users

    private val _groups = MutableStateFlow<List<GroupSearchResultDto>>(emptyList())
    val groups: StateFlow<List<GroupSearchResultDto>> = _groups

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun search(query: String) {
        if (query.isBlank()) {
            _messages.value = emptyList()
            _users.value = emptyList()
            _groups.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val response = chatApi.searchAll(query)
                if (response.isSuccessful) {
                    val results = response.body()
                    _messages.value = results?.messages ?: emptyList()
                    _users.value = results?.users ?: emptyList()
                    _groups.value = results?.groups ?: emptyList()
                } else {
                    _error.value = "Search failed: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
