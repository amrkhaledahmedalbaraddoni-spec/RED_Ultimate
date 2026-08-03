package com.red.feature.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.models.PublicUserDto
import com.red.feature.chat.BlockApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockListViewModel @Inject constructor(
    private val blockApi: BlockApi
) : ViewModel() {

    private val _blockedUsers = MutableStateFlow<List<PublicUserDto>>(emptyList())
    val blockedUsers: StateFlow<List<PublicUserDto>> = _blockedUsers

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = blockApi.getBlockedUsers()
                if (response.isSuccessful) {
                    _blockedUsers.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            try {
                val response = blockApi.blockUser(userId)
                if (response.isSuccessful) {
                    load() // Refresh list
                } else {
                    _error.value = "Failed to block user"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            try {
                val response = blockApi.unblockUser(userId)
                if (response.isSuccessful) {
                    _blockedUsers.value = _blockedUsers.value.filter { it.id != userId }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}
