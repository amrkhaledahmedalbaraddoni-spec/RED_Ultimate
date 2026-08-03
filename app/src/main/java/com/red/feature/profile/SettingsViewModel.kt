package com.red.feature.profile

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.ClientIdentity
import com.red.feature.auth.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@JsonClass(generateAdapter = true)
data class ProfileInfo(
    val fullName: String,
    val email: String,
    val phoneNumber: String
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val identity: ClientIdentity,
    private val authApi: AuthApi
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("red_settings", Context.MODE_PRIVATE)
    }

    private val _profile = MutableStateFlow(ProfileInfo("", "", ""))
    val profile: StateFlow<ProfileInfo> = _profile

    private val _passwordChangeResult = MutableStateFlow<String?>(null)
    val passwordChangeResult: StateFlow<String?> = _passwordChangeResult

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled

    private val _readReceiptsEnabled = MutableStateFlow(prefs.getBoolean("read_receipts_enabled", true))
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled

    private val _lastSeenEnabled = MutableStateFlow(prefs.getBoolean("last_seen_enabled", true))
    val lastSeenEnabled: StateFlow<Boolean> = _lastSeenEnabled

    private val _enterKeySends = MutableStateFlow(prefs.getBoolean("enter_key_sends", true))
    val enterKeySends: StateFlow<Boolean> = _enterKeySends

    private val _mediaAutoDownload = MutableStateFlow(prefs.getBoolean("media_auto_download", true))
    val mediaAutoDownload: StateFlow<Boolean> = _mediaAutoDownload

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

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun toggleSound(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun toggleReadReceipts(enabled: Boolean) {
        _readReceiptsEnabled.value = enabled
        prefs.edit().putBoolean("read_receipts_enabled", enabled).apply()
    }

    fun toggleLastSeen(enabled: Boolean) {
        _lastSeenEnabled.value = enabled
        prefs.edit().putBoolean("last_seen_enabled", enabled).apply()
    }

    fun toggleEnterKeySends(enabled: Boolean) {
        _enterKeySends.value = enabled
        prefs.edit().putBoolean("enter_key_sends", enabled).apply()
    }

    fun toggleMediaAutoDownload(enabled: Boolean) {
        _mediaAutoDownload.value = enabled
        prefs.edit().putBoolean("media_auto_download", enabled).apply()
    }
}
