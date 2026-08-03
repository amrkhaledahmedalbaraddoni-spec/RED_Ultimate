package com.red.core.crypto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Encryption indicator — shows the encryption status of a conversation.
 * Used in chat detail screen and conversation list.
 */
@Composable
fun EncryptionIndicator(
    isEncrypted: Boolean = true,
    isLocalEncryption: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isEncrypted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                else Color(0xFFFF9800).copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                null,
                modifier = Modifier.size(14.dp),
                tint = if (isEncrypted) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                if (isEncrypted) {
                    if (isLocalEncryption) "Local Encrypted" else "E2E Encrypted"
                } else "Not Encrypted",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isEncrypted) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
        }
    }
}

/**
 * Full encryption info dialog — explains the encryption status.
 */
@Composable
fun EncryptionInfoDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Encryption Status") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Encryption", fontWeight = FontWeight.Bold)
                }
                Text(
                    "Your messages are encrypted on this device using AES-256-GCM. " +
                    "This protects your messages at rest, but they are decrypted during transit.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EnhancedEncryption, null, tint = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End-to-End Encryption", fontWeight = FontWeight.Bold)
                }
                Text(
                    "End-to-end encryption (E2EE) using the Signal Protocol is planned for a future update. " +
                    "With E2EE, only you and the recipient can read your messages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}
