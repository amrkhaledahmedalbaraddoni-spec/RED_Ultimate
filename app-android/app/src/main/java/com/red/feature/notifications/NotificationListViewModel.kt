package com.red.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.feature.chat.NotificationApi
import com.red.feature.chat.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val notificationApi: NotificationApi
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationDto>>(emptyList())
    val notifications: StateFlow<List<NotificationDto>> = _notifications

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = notificationApi.getPendingNotifications()
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _notifications.value = list
                    _unreadCount.value = list.filter { !it.read }.size.toLong()
                }
            } catch (e: Exception) {
                _error.value = "Failed to load: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationApi.markAsRead(listOf(notificationId))
                _notifications.value = _notifications.value.map {
                    if (it.id == notificationId) it.copy(read = true) else it
                }
                _unreadCount.value = _notifications.value.count { !it.read }.toLong()
            } catch (_: Exception) { }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                notificationApi.markAllAsRead()
                _notifications.value = _notifications.value.map { it.copy(read = true) }
                _unreadCount.value = 0L
            } catch (_: Exception) { }
        }
    }
}
