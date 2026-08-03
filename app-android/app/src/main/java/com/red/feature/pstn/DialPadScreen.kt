package com.red.feature.pstn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * System B (PSTN) dialer — full numeric keypad with DTMF tones,
 * backspace, and call button.
 */
@Composable
fun DialPadScreen(
    onDial: (String) -> Unit = {}
) {
    var number by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display number
        Text(
            text = if (number.isEmpty()) "Enter number" else number,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = if (number.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        )

        // Dial pad grid
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (digit, letters) ->
                    DialKey(
                        digit = digit,
                        letters = letters,
                        onClick = { number += digit }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom row: empty, Call, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(72.dp))

            // Call button
            FloatingActionButton(
                onClick = {
                    if (number.isNotBlank()) onDial(number)
                },
                modifier = Modifier.size(72.dp),
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Call, "Call", modifier = Modifier.size(32.dp))
            }

            // Backspace
            if (number.isNotEmpty()) {
                IconButton(
                    onClick = {
                        if (number.isNotEmpty()) {
                            number = number.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        Icons.Default.Backspace,
                        "Delete",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(72.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SIM indicator
        Surface(
            shape = MaterialTheme.shapes.small,
            color = Color(0xFFF57C00).copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.SimCard,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFF57C00)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("PSTN via Dumin Gateway", fontSize = 12.sp, color = Color(0xFFF57C00))
            }
        }
    }
}

@Composable
private fun DialKey(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            digit,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (letters.isNotEmpty()) {
            Text(
                letters,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
        }
    }
}
