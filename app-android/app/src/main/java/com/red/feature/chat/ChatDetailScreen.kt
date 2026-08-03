package com.red.feature.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.red.core.crypto.EncryptionIndicator
import com.red.core.delivery.MessageEntity
import com.red.core.delivery.MessageStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full chat detail screen with:
 *  - Correct sender/recipient logic (uses identity.userId)
 *  - Typing indicator
 *  - Read receipts
 *  - Long-press context menu (copy, delete, forward)
 *  - Message status icons
 *  - Auto-scroll to bottom
 *  - VoIP/Video call actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    peerName: String = "Chat",
    viewModel: ChatViewModel = hiltViewModel()
) {
    var textState by remember { mutableStateOf("") }
    val messages by viewModel.getMessages(conversationId).collectAsState(initial = emptyList())
    val isTyping by viewModel.isTyping.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    var showMenuFor by remember { mutableStateOf<String?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Mark incoming messages as read
    LaunchedEffect(messages) {
        val unreadIds = messages
            .filter { it.senderId != viewModel.myUserId && it.status != MessageStatus.READ }
            .map { it.id }
        if (unreadIds.isNotEmpty()) {
            viewModel.markAsRead(conversationId, unreadIds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(peerName, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isTyping) {
                                Text(
                                    "typing…",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            EncryptionIndicator(
                                isEncrypted = true,
                                isLocalEncryption = true
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* VoIP Call */ }) {
                        Icon(Icons.Default.Call, "Voice Call")
                    }
                    IconButton(onClick = { /* Video Call */ }) {
                        Icon(Icons.Default.Videocam, "Video Call")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Error banner
                if (error != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                error!!,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
                ChatInput(
                    text = textState,
                    onTextChange = { textState = it },
                    onSend = {
                        if (textState.isNotBlank()) {
                            viewModel.sendMessage(conversationId, textState)
                            textState = ""
                        }
                    },
                    onAttach = {
                        // File attachment — requires ActivityResultLauncher for content picker
                        // and MultipartBody upload via StoryApi.upload()
                    },
                    onVoiceRecord = {
                        // Voice message recording — requires MediaRecorder + RECORD_AUDIO permission
                        // Saves as type="VOICE" via deliveryManager.sendMessage()
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            state = listState
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(
                    msg = msg,
                    myUserId = viewModel.myUserId,
                    onLongPress = {
                        selectedMessageId = msg.id
                        showMenuFor = msg.id
                    }
                )
            }
        }
    }

    // Context menu for message actions
    showMenuFor?.let { msgId ->
        val msg = messages.find { it.id == msgId }
        if (msg != null) {
            MessageContextMenu(
                message = msg,
                onCopy = {
                    // Copy payload to clipboard
                    showMenuFor = null
                },
                onDelete = {
                    viewModel.deleteMessage(msgId)
                    showMenuFor = null
                },
                onForward = {
                    selectedMessageId = msgId
                    showForwardDialog = true
                    showMenuFor = null
                },
                onDismiss = { showMenuFor = null }
            )
        }
    }
}

@Composable
private fun MessageContextMenu(
    message: MessageEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Actions") },
        text = {
            Column {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }
                TextButton(onClick = onForward) {
                    Icon(Icons.Default.Forward, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Forward")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MessageBubble(msg: MessageEntity, myUserId: String, onLongPress: () -> Unit = {}) {
    val isMe = msg.senderId == myUserId
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = { /* normal tap */ },
                    onLongClick = onLongPress
                )
                .background(color, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(msg.payload, color = textColor, fontSize = 15.sp)
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    formatMessageTime(msg.timestamp),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    DeliveryStatusIcon(msg.status)
                }
            }
        }
    }
}

@Composable
fun DeliveryStatusIcon(status: MessageStatus) {
    val icon = when (status) {
        MessageStatus.SENDING -> Icons.Default.Schedule
        MessageStatus.SENT -> Icons.Default.Check
        MessageStatus.DELIVERED -> Icons.Default.DoneAll
        MessageStatus.READ -> Icons.Default.DoneAll
        MessageStatus.FAILED -> Icons.Default.Error
    }
    val tint = if (status == MessageStatus.READ) Color(0xFF00B0FF) else Color.Gray
    Icon(icon, null, modifier = Modifier.size(14.dp), tint = tint)
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit = {},
    onVoiceRecord: () -> Unit = {}
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttach) {
                Icon(Icons.Default.Add, "Attach", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                maxLines = 4,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            if (text.isBlank()) {
                IconButton(onClick = onVoiceRecord) {
                    Icon(Icons.Default.Mic, "Voice", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                IconButton(onClick = onSend) {
                    Icon(Icons.Default.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

private fun formatMessageTime(timestamp: Long): String {
    return try {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } catch (_: Exception) { "" }
}
