package com.red.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.models.UserStatus
import com.red.core.models.UserView
import com.red.feature.auth.AuthApi
import com.red.feature.chat.BlockApi
import com.red.feature.chat.ChatApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String = "",
    val phoneNumber: String = "",
    val status: UserStatus = UserStatus.APPROVED,
    val role: String = "USER"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val chatApi: ChatApi,
    private val blockApi: BlockApi
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    private val _isBlocked = MutableStateFlow(false)
    val isBlocked: StateFlow<Boolean> = _isBlocked

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = chatApi.user(userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    _profile.value = UserProfile(
                        id = user?.id ?: userId,
                        fullName = user?.fullName ?: "",
                        status = user?.status ?: UserStatus.APPROVED
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to load profile: ${e.message}"
            }

            // Check if blocked
            try {
                val blockResponse = blockApi.isBlocked(userId)
                if (blockResponse.isSuccessful) {
                    _isBlocked.value = blockResponse.body()?.get("blocked") ?: false
                }
            } catch (_: Exception) { }

            _loading.value = false
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            try {
                blockApi.blockUser(userId)
                _isBlocked.value = true
            } catch (_: Exception) { }
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            try {
                blockApi.unblockUser(userId)
                _isBlocked.value = false
            } catch (_: Exception) { }
        }
    }
}
