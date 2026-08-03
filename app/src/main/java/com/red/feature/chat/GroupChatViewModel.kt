package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.ClientIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupChatViewModel @Inject constructor(
    private val groupApi: GroupApi,
    private val contactApi: ContactApi,
    private val identity: ClientIdentity
) : ViewModel() {

    private val _groups = MutableStateFlow<List<GroupDto>>(emptyList())
    val groups: StateFlow<List<GroupDto>> = _groups

    private val _currentGroup = MutableStateFlow<GroupDto?>(null)
    val currentGroup: StateFlow<GroupDto?> = _currentGroup

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = groupApi.listGroups()
                if (response.isSuccessful) {
                    _groups.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load groups: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGroup(name: String, description: String, memberIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = groupApi.createGroup(
                    CreateGroupRequest(name = name, description = description, memberIds = memberIds)
                )
                if (response.isSuccessful) {
                    val group = response.body()
                    if (group != null) {
                        _groups.value = _groups.value + group
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to create group: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            try {
                val response = groupApi.getGroup(groupId)
                if (response.isSuccessful) {
                    _currentGroup.value = response.body()
                } else {
                    _error.value = "Failed to load group: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun addMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = groupApi.addMember(groupId, AddMemberRequest(userId))
                if (response.isSuccessful) {
                    loadGroup(groupId) // Refresh
                } else {
                    _error.value = "Failed to add member: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun removeMember(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = groupApi.removeMember(groupId, userId)
                if (response.isSuccessful) {
                    loadGroup(groupId) // Refresh
                } else {
                    _error.value = "Failed to remove member: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun promoteAdmin(groupId: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = groupApi.promoteAdmin(groupId, PromoteAdminRequest(userId))
                if (response.isSuccessful) {
                    loadGroup(groupId) // Refresh
                } else {
                    _error.value = "Failed to promote admin: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun deleteGroup(groupId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = groupApi.deleteGroup(groupId)
                if (response.isSuccessful) {
                    _groups.value = _groups.value.filter { it.id != groupId }
                    _currentGroup.value = null
                    onSuccess()
                } else {
                    _error.value = "Failed to delete group: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
