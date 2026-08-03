package com.red.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.MessageDao
import com.red.core.delivery.MessageDeliveryManager
import com.red.core.delivery.MessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageDao: MessageDao,
    private val deliveryManager: MessageDeliveryManager
) : ViewModel() {

    fun getMessages(conversationId: String): StateFlow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendMessage(conversationId: String, text: String) {
        viewModelScope.launch {
            deliveryManager.sendMessage(conversationId, conversationId, text)
        }
    }
}
