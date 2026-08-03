package com.red.feature.pstn

import androidx.compose.animation.core.*
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
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * PSTN call screen with:
 *  - Auto-dial on start
 *  - Call duration timer
 *  - Speaker/mute toggle
 *  - Hangup button
 *  - Call status indicator
 */
@Composable
fun PstnCallScreen(
    phoneNumber: String,
    viewModel: PstnViewModel = hiltViewModel(),
    onCallEnded: () -> Unit
) {
    val callState by viewModel.callState.collectAsState()

    // Auto-dial when screen opens
    LaunchedEffect(phoneNumber) {
        viewModel.makeCall(phoneNumber)
    }

    // Timer for active call
    var elapsedSeconds by remember { mutableStateOf(0L) }
    LaunchedEffect(callState) {
        if (callState is PstnCallState.Active) {
            elapsedSeconds = (callState as PstnCallState.Active).duration
        }
    }

    // Navigate away when call ends
    LaunchedEffect(callState) {
        if (callState is PstnCallState.Ended) {
            kotlinx.coroutines.delay(2000) // Show "Ended" for 2 seconds
            onCallEnded()
        }
    }

    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsing phone icon
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Surface(
                modifier = Modifier.size(120.dp * scale),
                shape = CircleShape,
                color = when (callState) {
                    is PstnCallState.Active -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                    is PstnCallState.Ended -> Color(0xFFF44336).copy(alpha = 0.2f)
                    else -> Color(0xFFF57C00).copy(alpha = 0.2f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Phone,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = when (callState) {
                            is PstnCallState.Active -> Color(0xFF4CAF50)
                            is PstnCallState.Ended -> Color(0xFFF44336)
                            else -> Color(0xFFF57C00)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                phoneNumber,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statusText = when (callState) {
                is PstnCallState.Dialing -> "Dialing via Dumin…"
                is PstnCallState.Ringing -> "Ringing…"
                is PstnCallState.Active -> {
                    val duration = (callState as PstnCallState.Active).duration
                    formatDuration(duration)
                }
                is PstnCallState.Ended -> "Call Ended"
                else -> "Connecting…"
            }

            Text(
                statusText,
                fontSize = 18.sp,
                color = when (callState) {
                    is PstnCallState.Active -> Color(0xFF4CAF50)
                    is PstnCallState.Ended -> Color(0xFFF44336)
                    else -> Color(0xFFF57C00)
                },
                fontWeight = FontWeight.Medium
            )

            // SIM status indicator
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(0xFFF57C00).copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SimCard, null, modifier = Modifier.size(16.dp), tint = Color(0xFFF57C00))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PSTN · Dumin Gateway", fontSize = 12.sp, color = Color(0xFFF57C00))
                }
            }
        }

        // Call controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
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
        }

        // Hang up button
        FloatingActionButton(
            onClick = {
                viewModel.hangup()
                onCallEnded()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .size(72.dp),
            containerColor = Color.Red,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(32.dp))
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
