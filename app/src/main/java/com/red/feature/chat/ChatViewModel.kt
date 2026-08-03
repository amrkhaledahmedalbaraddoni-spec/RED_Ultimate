package com.red.feature.chat

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.MessageDao
import com.red.core.delivery.MessageDeliveryManager
import com.red.core.delivery.MessageEntity
import com.red.core.delivery.MessageStatus
import com.red.core.delivery.ClientIdentity
import com.red.core.security.SessionManager
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
    private val identity: ClientIdentity,
    private val sessionManager: SessionManager,
    private val chatApi: ChatApi,
    private val reactionApi: ReactionApi,
    private val pinApi: PinApi
) : ViewModel() {

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val _peerOnline = MutableStateFlow(false)
    val peerOnline: StateFlow<Boolean> = _peerOnline

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _conversations = MutableStateFlow<List<ConversationDto>>(emptyList())
    val conversations: StateFlow<List<ConversationDto>> = _conversations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

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
                // The RED backend currently has no shared-key exchange. Do not encrypt the
                // network payload with a device-local key: the recipient could not decrypt it.
                // Signal conversations continue to use Signal's native E2EE path.
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

    /** Forward a message to a different conversation. */
    fun forwardMessage(messageId: String, targetConversationId: String) {
        viewModelScope.launch {
            try {
                val message = messageDao.getMessageById(messageId)
                if (message != null) {
                    val payload = displayPayload(message)
                    deliveryManager.sendMessage(targetConversationId, targetConversationId, payload)
                }
            } catch (e: Exception) {
                _error.value = "Failed to forward: ${e.message}"
            }
        }
    }

    /** Send a reply/quote to a specific message. */
    fun sendReply(conversationId: String, receiverId: String, text: String, replyToMessageId: String) {
        viewModelScope.launch {
            try {
                val replyText = "↩ Reply to $replyToMessageId:\n$text"
                deliveryManager.sendMessage(conversationId, receiverId, replyText)
            } catch (e: Exception) {
                _error.value = "Failed to send reply: ${e.message}"
            }
        }
    }

    /** Load conversations from the API. */
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Conversations are loaded via ChatListViewModel, but this is a fallback
            } catch (e: Exception) {
                _error.value = "Failed to load conversations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
            try {
                val response = chatApi.deleteMessage(messageId)
                if (response.isSuccessful) {
                    messageDao.deleteMessage(messageId)
                } else {
                    _error.value = "Unable to delete message (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = "Unable to delete message: ${e.message}"
            }
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            runCatching { reactionApi.addReaction(AddReactionRequest(messageId, emoji)) }
                .onFailure { _error.value = "Unable to add reaction: ${it.message}" }
        }
    }

    fun pinMessage(messageId: String, conversationId: String) {
        viewModelScope.launch {
            runCatching { pinApi.pinMessage(PinMessageRequest(messageId, conversationId)) }
                .onFailure { _error.value = "Unable to pin message: ${it.message}" }
        }
    }

    fun displayPayload(message: MessageEntity): String =
        runCatching { sessionManager.decryptMessage(message.payload) }
            .getOrDefault(message.payload)

    fun sendAttachment(conversationId: String, receiverId: String, bytes: ByteArray, type: String) {
        viewModelScope.launch {
            try {
                require(bytes.size <= MAX_ATTACHMENT_BYTES) { "Attachment is larger than 10 MB" }
                val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                deliveryManager.sendMessage(
                    conversationId,
                    receiverId,
                    encoded,
                    type
                )
            } catch (e: Exception) {
                _error.value = "Failed to send attachment: ${e.message}"
            }
        }
    }

    fun setTyping(typing: Boolean) {
        _isTyping.value = typing
        // Typing indicator sent via WebSocket TYPING frame in MessageDeliveryManager
    }

    fun clearError() {
        _error.value = null
    }

    private companion object {
        const val MAX_ATTACHMENT_BYTES = 10 * 1024 * 1024
    }
}
