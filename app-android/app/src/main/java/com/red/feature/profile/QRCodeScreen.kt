package com.red.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QR Code screen — shows the user's QR code for sharing
 * and allows scanning other users' QR codes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    userId: String,
    userName: String,
    onBack: () -> Unit = {},
    onScanResult: (String) -> Unit = {}
) {
    var showScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Your QR Code",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Scan this code to add $userName as a contact",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // QR Code placeholder (would use a QR library in production)
            Surface(
                modifier = Modifier.size(250.dp),
                color = Color.White,
                shape = MaterialTheme.shapes.large
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.QrCode2,
                            null,
                            modifier = Modifier.size(200.dp),
                            tint = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User ID display
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "ID: $userId",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Scan button
            OutlinedButton(
                onClick = { showScanner = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR Code")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share button
            OutlinedButton(
                onClick = { /* Share QR code */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }
    }

    // Scanner dialog
    if (showScanner) {
        AlertDialog(
            onDismissRequest = { showScanner = false },
            title = { Text("Scan QR Code") },
            text = {
                Text("Point your camera at a QR code to add a contact. Camera permission required.")
            },
            confirmButton = {
                TextButton(onClick = { showScanner = false }) { Text("OK") }
            }
        )
    }
}
