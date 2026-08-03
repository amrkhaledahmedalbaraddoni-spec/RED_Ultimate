package com.red.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Video call screen — placeholder for WebRTC-based video calling.
 * Shows a local/remote video view and call controls.
 */
@Composable
fun VideoCallScreen(
    peerName: String = "User",
    onEndCall: () -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Remote video placeholder
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            peerName.take(2).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    peerName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Connecting…",
                    color = Color(0xFF4CAF50),
                    fontSize = 16.sp
                )
            }
        }

        // Local video preview (small)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(120.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF333333)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCameraOff) {
                    Icon(
                        Icons.Default.VideocamOff,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Call controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mute
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = { isMuted = !isMuted },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isMuted) Color.White else Color(0xFF333333)
                    )
                ) {
                    Icon(
                        if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        "Mute",
                        tint = if (isMuted) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Mute", color = Color.White, fontSize = 12.sp)
            }

            // Camera toggle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = { isCameraOff = !isCameraOff },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isCameraOff) Color.White else Color(0xFF333333)
                    )
                ) {
                    Icon(
                        if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        "Camera",
                        tint = if (isCameraOff) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Camera", color = Color.White, fontSize = 12.sp)
            }

            // Speaker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isSpeakerOn) Color.White else Color(0xFF333333)
                    )
                ) {
                    Icon(
                        if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        "Speaker",
                        tint = if (isSpeakerOn) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Speaker", color = Color.White, fontSize = 12.sp)
            }

            // Switch camera
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FilledIconButton(
                    onClick = { /* TODO: Switch camera */ },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF333333)
                    )
                ) {
                    Icon(Icons.Default.Cached, "Flip", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Flip", color = Color.White, fontSize = 12.sp)
            }
        }

        // End call button
        FloatingActionButton(
            onClick = onEndCall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .size(72.dp),
            containerColor = Color.Red,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(32.dp))
        }
    }
}
