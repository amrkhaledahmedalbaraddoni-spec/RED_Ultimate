package com.red.feature.chat

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.core.content.ContextCompat
import android.widget.Toast
import java.io.File
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
    peerId: String = "",
    peerName: String = "Chat",
    onVoiceCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onAttach: () -> Unit = {},
    onVoiceRecord: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val receiverId = peerId.ifBlank { conversationId }
    var textState by remember { mutableStateOf("") }
    val messages by viewModel.getMessages(conversationId).collectAsState(initial = emptyList())
    val isTyping by viewModel.isTyping.collectAsState()
    val error by viewModel.error.collectAsState()
    val listState = rememberLazyListState()
    var showMenuFor by remember { mutableStateOf<String?>(null) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    val attachmentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                val type = when (context.contentResolver.getType(uri)?.substringBefore('/')) {
                    "image" -> "IMAGE"
                    "video" -> "VIDEO"
                    else -> "FILE"
                }
                viewModel.sendAttachment(conversationId, receiverId, bytes, type)
            }
        }
    }
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }

    fun startVoiceRecording() {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        runCatching {
            newRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            newRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            newRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            newRecorder.setOutputFile(file.absolutePath)
            newRecorder.prepare()
            newRecorder.start()
            recorder = newRecorder
            recordingFile = file
            isRecording = true
        }.onFailure {
            newRecorder.release()
            Toast.makeText(context, "Unable to start voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopVoiceRecording() {
        val currentRecorder = recorder ?: return
        val file = recordingFile
        runCatching { currentRecorder.stop() }
            .onSuccess {
                file?.takeIf { it.exists() }?.let { voiceFile ->
                    viewModel.sendAttachment(conversationId, receiverId, voiceFile.readBytes(), "VOICE")
                    voiceFile.delete()
                }
            }
            .onFailure { Toast.makeText(context, "Voice recording was too short", Toast.LENGTH_SHORT).show() }
        currentRecorder.release()
        recorder = null
        recordingFile = null
        isRecording = false
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceRecording()
        else Toast.makeText(context, "Microphone permission is required", Toast.LENGTH_SHORT).show()
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.let { runCatching { it.stop() } }
            recorder?.release()
            recordingFile?.delete()
        }
    }

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
                    IconButton(onClick = onVoiceCall) {
                        Icon(Icons.Default.Call, "Voice Call")
                    }
                    IconButton(onClick = onVideoCall) {
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
                            viewModel.sendMessage(conversationId, receiverId, textState)
                            textState = ""
                        }
                    },
                    onAttach = {
                        onAttach()
                        attachmentLauncher.launch("*/*")
                    },
                    isRecording = isRecording,
                    onVoiceRecord = {
                        onVoiceRecord()
                        if (isRecording) {
                            stopVoiceRecording()
                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            startVoiceRecording()
                        } else {
                            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
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
                    text = viewModel.displayPayload(msg),
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
                messageText = viewModel.displayPayload(msg),
                onCopy = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("RED message", viewModel.displayPayload(msg)))
                    showMenuFor = null
                },
                onReaction = { emoji ->
                    viewModel.addReaction(msgId, emoji)
                    showMenuFor = null
                },
                onPin = {
                    viewModel.pinMessage(msgId, conversationId)
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

    if (showForwardDialog && selectedMessageId != null) {
        ForwardMessageDialog(
            onDismiss = {
                showForwardDialog = false
                selectedMessageId = null
            },
            onForward = { targetConversationId ->
                viewModel.forwardMessage(selectedMessageId!!, targetConversationId)
                showForwardDialog = false
                selectedMessageId = null
            }
        )
    }
}

@Composable
private fun ForwardMessageDialog(
    onDismiss: () -> Unit,
    onForward: (String) -> Unit
) {
    var targetConversationId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forward message") },
        text = {
            OutlinedTextField(
                value = targetConversationId,
                onValueChange = { targetConversationId = it },
                label = { Text("Target conversation ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onForward(targetConversationId.trim()) },
                enabled = targetConversationId.isNotBlank()
            ) { Text("Forward") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun MessageContextMenu(
    message: MessageEntity,
    messageText: String,
    onCopy: () -> Unit,
    onReaction: (String) -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onForward: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Message Actions") },
        text = {
            Column {
                Text(
                    messageText,
                    maxLines = 3,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Quick reaction row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (emoji in listOf("👍", "❤️", "😂", "😮", "😢", "🙏")) {
                        TextButton(onClick = { onReaction(emoji) }) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
                HorizontalDivider()
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
                TextButton(onClick = onPin) {
                    Icon(Icons.Default.PushPin, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pin")
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
fun MessageBubble(
    msg: MessageEntity,
    text: String = msg.payload,
    myUserId: String,
    onLongPress: () -> Unit = {}
) {
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
            // Message type indicator for non-text
            if (msg.type != "TEXT") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (msg.type) {
                            "IMAGE" -> Icons.Default.Image
                            "VIDEO" -> Icons.Default.Videocam
                            "FILE" -> Icons.Default.InsertDriveFile
                            "VOICE" -> Icons.Default.Mic
                            else -> Icons.Default.Message
                        },
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        msg.type,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(text, color = textColor, fontSize = 15.sp)
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
    isRecording: Boolean = false,
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
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        if (isRecording) "Stop recording" else "Voice",
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
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
