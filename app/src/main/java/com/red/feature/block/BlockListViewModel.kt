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

    private val _blockedIds = MutableStateFlow<List<String>>(emptyList())
    val blockedIds: StateFlow<List<String>> = _blockedIds

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
                    // Backend returns List<String> of blocked user IDs
                    val ids = response.body() ?: emptyList()
                    _blockedIds.value = ids
                    // Convert to PublicUserDto for UI display
                    _blockedUsers.value = ids.map { id ->
                        PublicUserDto(id = id, fullName = id.take(8) + "…", status = com.red.core.models.UserStatus.APPROVED)
                    }
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
                val response = blockApi.blockUser(mapOf("blockeeId" to userId))
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
                    _blockedIds.value = _blockedIds.value.filter { it != userId }
                    _blockedUsers.value = _blockedUsers.value.filter { it.id != userId }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}
