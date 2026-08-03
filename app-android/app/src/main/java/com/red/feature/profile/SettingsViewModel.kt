package com.red.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.ClientIdentity
import com.red.feature.auth.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileInfo(
    val fullName: String,
    val email: String,
    val phoneNumber: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val identity: ClientIdentity,
    private val authApi: AuthApi
) : ViewModel() {

    private val _profile = MutableStateFlow(ProfileInfo("", "", ""))
    val profile: StateFlow<ProfileInfo> = _profile

    private val _passwordChangeResult = MutableStateFlow<String?>(null)
    val passwordChangeResult: StateFlow<String?> = _passwordChangeResult

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = authApi.getMyProfile()
                if (response.isSuccessful) {
                    val user = response.body()
                    _profile.value = ProfileInfo(
                        fullName = user?.fullName ?: "",
                        email = user?.email ?: "",
                        phoneNumber = user?.phoneNumber ?: ""
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun updateProfile(name: String, phone: String) {
        viewModelScope.launch {
            try {
                val response = authApi.updateProfile(mapOf("fullName" to name, "phoneNumber" to phone))
                if (response.isSuccessful) {
                    _profile.value = _profile.value.copy(fullName = name, phoneNumber = phone)
                }
            } catch (_: Exception) { }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val response = authApi.changePassword(mapOf(
                    "currentPassword" to currentPassword,
                    "newPassword" to newPassword
                ))
                _passwordChangeResult.value = if (response.isSuccessful) "Password changed successfully" else "Failed to change password"
            } catch (e: Exception) {
                _passwordChangeResult.value = "Error: ${e.message}"
            }
        }
    }
}
