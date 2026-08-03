package com.red.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.auth.TokenStore
import com.red.core.delivery.ClientIdentity
import com.red.core.delivery.MessageDeliveryManager
import com.red.core.models.UserStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Pending : AuthUiState()
    object Authenticated : AuthUiState()
    object Rejected : AuthUiState()
    object Banned : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authApi: AuthApi,
    private val identity: ClientIdentity,
    private val deliveryManager: MessageDeliveryManager,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        val savedToken = tokenStore.getToken()
        val savedUserId = tokenStore.getUserId()
        if (!savedToken.isNullOrBlank() && !savedUserId.isNullOrBlank()) {
            identity.token = savedToken
            identity.userId = savedUserId
            checkStatus()
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authApi.register(mapOf("fullName" to name, "email" to email, "password" to password))
                if (response.isSuccessful) {
                    _uiState.value = AuthUiState.Pending
                } else {
                    _uiState.value = AuthUiState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = authApi.login(mapOf("email" to email, "password" to password))
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body == null) {
                        _uiState.value = AuthUiState.Error("Login returned an empty response")
                        return@launch
                    }
                    when (body.user.status) {
                        UserStatus.APPROVED -> {
                            identity.userId = body.user.id
                            identity.token = body.token
                            tokenStore.saveToken(body.token, body.user.id)
                            org.thoughtcrime.securesms.developed.MasterIntegration.storeToken(context, body.token)
                            org.thoughtcrime.securesms.developed.MasterIntegration.markApproved(context)
                            org.thoughtcrime.securesms.developed.REDCore.initializeEverything(context)
                            runCatching { deliveryManager.start() }
                            _uiState.value = AuthUiState.Authenticated
                        }
                        UserStatus.PENDING -> _uiState.value = AuthUiState.Pending
                        UserStatus.REJECTED -> _uiState.value = AuthUiState.Rejected
                        UserStatus.BANNED -> _uiState.value = AuthUiState.Banned
                        else -> _uiState.value = AuthUiState.Error("Unknown user status")
                    }
                } else {
                    _uiState.value = AuthUiState.Error("Login failed")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun checkStatus() {
        viewModelScope.launch {
            try {
                val response = authApi.getStatus()
                if (response.isSuccessful) {
                    when (response.body()?.status) {
                        UserStatus.APPROVED -> {
                            org.thoughtcrime.securesms.developed.MasterIntegration.markApproved(context)
                            if (identity.token.isNotBlank()) {
                                org.thoughtcrime.securesms.developed.MasterIntegration.storeToken(context, identity.token)
                            }
                            org.thoughtcrime.securesms.developed.REDCore.initializeEverything(context)
                            runCatching { deliveryManager.start() }
                            _uiState.value = AuthUiState.Authenticated
                        }
                        UserStatus.PENDING -> _uiState.value = AuthUiState.Pending
                        UserStatus.REJECTED -> _uiState.value = AuthUiState.Rejected
                        UserStatus.BANNED -> _uiState.value = AuthUiState.Banned
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                // Ignore status check errors
            }
        }
    }

    fun logout() {
        deliveryManager.stop()
        org.thoughtcrime.securesms.developed.REDCore.reset()
        tokenStore.clear()
        org.thoughtcrime.securesms.developed.MasterIntegration.clearApproval(context)
        identity.userId = ""
        identity.token = ""
        _uiState.value = AuthUiState.Idle
    }
}
