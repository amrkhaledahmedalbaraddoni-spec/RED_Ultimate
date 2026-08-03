package com.red.feature.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.red.core.delivery.ClientIdentity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user emoji status.
 */
@HiltViewModel
class UserStatusViewModel @Inject constructor(
    private val userStatusApi: UserStatusApi,
    private val identity: ClientIdentity
) : ViewModel() {

    private val _currentStatus = MutableStateFlow<UserStatusDto?>(null)
    val currentStatus: StateFlow<UserStatusDto?> = _currentStatus

    private val _isSetting = MutableStateFlow(false)
    val isSetting: StateFlow<Boolean> = _isSetting

    fun setStatus(emoji: String, text: String = "") {
        viewModelScope.launch {
            _isSetting.value = true
            try {
                val response = userStatusApi.setStatus(SetStatusRequest(emoji, text))
                if (response.isSuccessful) {
                    _currentStatus.value = response.body()
                }
            } catch (_: Exception) { }
            _isSetting.value = false
        }
    }

    fun clearStatus() {
        viewModelScope.launch {
            try {
                userStatusApi.clearStatus()
                _currentStatus.value = null
            } catch (_: Exception) { }
        }
    }
}

/**
 * Status update screen — pick an emoji and optional text for your status.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateScreen(
    onBack: () -> Unit = {},
    viewModel: UserStatusViewModel = hiltViewModel()
) {
    val currentStatus by viewModel.currentStatus.collectAsState()
    val isSetting by viewModel.isSetting.collectAsState()
    var statusText by remember { mutableStateOf("") }

    val statusEmojis = listOf(
        "😊", "🔥", "💼", "🎓", "🎉", "❤️",
        "🌟", "🎵", "📚", "🏃", "✈️", "🏠",
        "☕", "🎮", "💤", "🤔", "💪", "🧘",
        "🏖️", "🍕", "📸", "💡", "🔑", "🎯"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Update Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (currentStatus != null) {
                        TextButton(onClick = { viewModel.clearStatus() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Current status preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                currentStatus?.emoji ?: "😊",
                                fontSize = androidx.compose.ui.unit.TextUnit(28f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "My Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (currentStatus?.text.isNullOrBlank()) "Tap to set status" else currentStatus!!.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status text input
            OutlinedTextField(
                value = statusText,
                onValueChange = { statusText = it },
                label = { Text("What's your status?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emoji grid
            Text(
                "Choose an emoji",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(statusEmojis) { emoji ->
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                viewModel.setStatus(emoji, statusText)
                            },
                        shape = CircleShape,
                        color = if (currentStatus?.emoji == emoji)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = androidx.compose.ui.unit.TextUnit(24f))
                        }
                    }
                }
            }

            if (isSetting) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
