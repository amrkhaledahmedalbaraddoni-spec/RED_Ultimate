package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.ClientIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationItem(
    val conversationId: String,
    val peerName: String,
    val lastTimestamp: Long,
    val messageCount: Long
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatApi: ChatApi,
    private val identity: ClientIdentity
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ConversationItem>>(emptyList())
    val conversations: StateFlow<List<ConversationItem>> = _conversations

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = chatApi.conversations()
                if (response.isSuccessful) {
                    val dtos = response.body() ?: emptyList()
                    val items = dtos.map { dto ->
                        val peerName = try {
                            chatApi.user(dto.peerId).body()?.fullName ?: dto.peerId
                        } catch (_: Exception) { dto.peerId }
                        ConversationItem(dto.conversationId, peerName, dto.lastTimestamp, dto.messageCount)
                    }
                    _conversations.value = items
                }
            } catch (_: Exception) { }
            _loading.value = false
        }
    }
}
