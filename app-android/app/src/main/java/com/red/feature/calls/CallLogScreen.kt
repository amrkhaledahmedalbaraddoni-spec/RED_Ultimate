package com.red.feature.calls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.red.feature.pstn.PstnCallLog
import java.text.SimpleDateFormat
import java.util.*

/**
 * VoIP/System A call log. Shows recent PSTN and VoIP calls with
 * duration, direction, and status information.
 */
@Composable
fun CallLogScreen(
    onCallClick: ((String) -> Unit)? = null,
    viewModel: CallLogViewModel = hiltViewModel()
) {
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Calls") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No recent calls",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Your call history will appear here",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(callLogs) { call ->
                    CallLogItem(call, onCallClick)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CallLogItem(
    call: PstnCallLog,
    onCallClick: ((String) -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCallClick?.invoke(call.phoneNumber) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Direction icon
        val icon = when (call.direction) {
            "OUTGOING" -> Icons.Default.CallMade
            "INCOMING" -> Icons.Default.CallReceived
            "MISSED" -> Icons.Default.CallEnd
            else -> Icons.Default.Call
        }
        val iconColor = when (call.direction) {
            "MISSED" -> MaterialTheme.colorScheme.error
            "INCOMING" -> Color(0xFF4CAF50)
            else -> MaterialTheme.colorScheme.primary
        }
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = iconColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                call.phoneNumber,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (call.direction == "MISSED") FontWeight.Bold else FontWeight.Normal
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatCallDirection(call.direction),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (call.direction == "MISSED") MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (call.duration > 0) {
                    Text(
                        " · ${formatDuration(call.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            formatCallTimestamp(call.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatCallDirection(direction: String): String = when (direction) {
    "OUTGOING" -> "Outgoing"
    "INCOMING" -> "Incoming"
    "MISSED" -> "Missed"
    else -> direction
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
}

private fun formatCallTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
