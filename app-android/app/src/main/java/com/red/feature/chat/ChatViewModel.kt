package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.MessageDao
import com.red.core.delivery.MessageDeliveryManager
import com.red.core.delivery.MessageEntity
import com.red.core.delivery.MessageStatus
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

    private val _peerOnline = MutableStateFlow(false)
    val peerOnline: StateFlow<Boolean> = _peerOnline

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** My user ID from the identity store — used for correct sender/recipient logic. */
    val myUserId: String get() = identity.userId

    fun getMessages(conversationId: String): StateFlow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Send a text message.  [receiverId] is the peer's user ID (NOT the conversation ID).
     * The conversation ID is derived from the two user IDs.
     */
    fun sendMessage(conversationId: String, receiverId: String, text: String) {
        viewModelScope.launch {
            try {
                deliveryManager.sendMessage(conversationId, receiverId, text)
            } catch (e: Exception) {
                _error.value = "Failed to send: ${e.message}"
            }
        }
    }

    /** Overload that keeps backward compatibility — treats conversationId as the peer. */
    fun sendMessage(conversationId: String, text: String) {
        sendMessage(conversationId, conversationId, text)
    }

    fun markAsRead(conversationId: String, messageIds: List<String>) {
        viewModelScope.launch {
            for (id in messageIds) {
                messageDao.updateStatus(id, MessageStatus.READ)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            // Mark as failed locally; server deletion would be a separate API call
            messageDao.updateStatus(messageId, MessageStatus.FAILED)
        }
    }

    fun setTyping(typing: Boolean) {
        _isTyping.value = typing
        // TODO: Send typing indicator via WebSocket TYPING frame
    }

    fun clearError() {
        _error.value = null
    }
}
