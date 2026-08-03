package com.red.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * App lock screen — shown when biometric authentication is enabled.
 * Displays a lock icon and unlock button.
 */
@Composable
fun AppLockScreen(
    onUnlock: () -> Unit = {},
    onUnlockWithBiometric: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lock icon
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "RED is Locked",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Authenticate to access your messages",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Biometric unlock button
        Button(
            onClick = onUnlockWithBiometric,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Fingerprint, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Unlock with Biometric")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // PIN fallback
        OutlinedButton(
            onClick = onUnlock,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Pin, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Unlock with PIN")
        }
    }
}
