package com.red.feature.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.database.ContactDao
import com.red.core.database.ContactEntity
import com.red.feature.chat.ChatApi
import com.red.feature.chat.ContactApi
import com.red.core.models.PublicUserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val contactApi: ContactApi,
    private val chatApi: ChatApi
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val contacts: StateFlow<List<ContactEntity>> = _contacts

    private val _searchResults = MutableStateFlow<List<ContactEntity>>(emptyList())
    val searchResults: StateFlow<List<ContactEntity>> = _searchResults

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            // Load from local DB first
            contactDao.getAllContacts().collect { local ->
                _contacts.value = local
            }
            // Then sync from server
            syncFromServer()
            _loading.value = false
        }
    }

    private suspend fun syncFromServer() {
        try {
            val response = contactApi.getContacts()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { dto ->
                    ContactEntity(
                        userId = dto.id,
                        fullName = dto.fullName,
                        email = "" // PublicUserDto doesn't have email
                    )
                }
                contactDao.upsertAll(entities)
            }
        } catch (_: Exception) { }
    }

    fun search(query: String) {
        viewModelScope.launch {
            contactDao.searchContacts(query).collect { results ->
                _searchResults.value = results
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    fun addContact(email: String) {
        viewModelScope.launch {
            try {
                val response = contactApi.addContact(mapOf("email" to email))
                if (response.isSuccessful) {
                    syncFromServer()
                } else {
                    _error.value = "Failed to add contact"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun removeContact(userId: String) {
        viewModelScope.launch {
            try {
                contactApi.removeContact(userId)
                contactDao.deleteContact(userId)
            } catch (_: Exception) { }
        }
    }
}
