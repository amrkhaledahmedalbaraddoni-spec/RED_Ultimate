package com.red.feature.pstn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * System B (PSTN) dialer. Collects a phone number and routes to the PSTN call screen.
 */
@Composable
fun DialPadScreen(
  onDial: (String) -> Unit = {}
) {
  var number by remember { mutableStateOf("") }

  Scaffold(topBar = { TopAppBar(title = { Text("Phone (Dumin)") }) }) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      OutlinedTextField(
        value = number,
        onValueChange = { number = it.filter { ch -> ch.isDigit() || ch == '+' } },
        label = { Text("Phone number") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = Modifier.fillMaxWidth()
      )
      Button(
        onClick = { if (number.isNotBlank()) onDial(number) },
        modifier = Modifier.padding(top = 24.dp)
      ) {
        Icon(Icons.Default.Call, contentDescription = null)
        Text("  Call via Dumin")
      }
    }
  }
}
