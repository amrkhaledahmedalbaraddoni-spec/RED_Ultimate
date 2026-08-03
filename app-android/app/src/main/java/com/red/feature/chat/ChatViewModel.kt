package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.MessageDao
import com.red.core.delivery.MessageDeliveryManager
import com.red.core.delivery.MessageEntity
import com.red.core.delivery.ClientIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageDao: MessageDao,
    private val deliveryManager: MessageDeliveryManager,
    private val identity: ClientIdentity
) : ViewModel() {

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun getMessages(conversationId: String): StateFlow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(conversationId: String, text: String) {
        viewModelScope.launch {
            deliveryManager.sendMessage(conversationId, conversationId, text)
        }
    }

    fun markAsRead(conversationId: String, messageIds: List<String>) {
        viewModelScope.launch {
            for (id in messageIds) {
                messageDao.updateStatus(id, com.red.core.delivery.MessageStatus.READ)
            }
        }
    }

    fun setTyping(typing: Boolean) {
        _isTyping.value = typing
        // TODO: Send typing indicator via WebSocket
    }
}
