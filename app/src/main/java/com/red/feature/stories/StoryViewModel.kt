package com.red.feature.stories

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storyApi: StoryApi
) : ViewModel() {

    private val _stories = MutableStateFlow<List<StoryDto>>(emptyList())
    val stories: StateFlow<List<StoryDto>> = _stories

    private val _uploading = MutableStateFlow(false)
    val uploading: StateFlow<Boolean> = _uploading

    fun load() {
        viewModelScope.launch {
            try {
                val response = storyApi.list()
                if (response.isSuccessful) {
                    _stories.value = response.body() ?: emptyList()
                }
            } catch (_: Exception) { }
        }
    }

    fun publish(uri: Uri) {
        viewModelScope.launch {
            _uploading.value = true
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
                val part = MediaParts.part(bytes)
                val uploadResp = storyApi.upload(part)
                if (uploadResp.isSuccessful) {
                    val mediaUrl = uploadResp.body()?.downloadUrl ?: return@launch
                    storyApi.create(StoryCreateRequest(mediaUrl))
                    load()
                }
            } catch (_: Exception) { }
            _uploading.value = false
        }
    }
}
