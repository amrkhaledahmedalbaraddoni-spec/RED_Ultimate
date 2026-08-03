package com.red.feature.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.MessageDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    private val messageDao: MessageDao
) : ViewModel() {

    private val _mediaItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val mediaItems: StateFlow<List<MediaItem>> = _mediaItems

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun load(conversationId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                messageDao.getMessagesForConversation(conversationId).collect { messages ->
                    _mediaItems.value = messages
                        .filter { it.type == "IMAGE" || it.type == "VIDEO" || it.type == "FILE" }
                        .map { msg ->
                            MediaItem(
                                id = msg.id,
                                type = msg.type,
                                url = msg.payload,
                                name = msg.payload.substringAfterLast("/"),
                                size = "", // Would need server metadata
                                timestamp = msg.timestamp
                            )
                        }
                }
            } catch (_: Exception) { }
            _loading.value = false
        }
    }
}
