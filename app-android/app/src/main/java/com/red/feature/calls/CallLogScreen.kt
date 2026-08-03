package com.red.feature.calls

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** VoIP/System A call log placeholder. */
@Composable
fun CallLogScreen() {
  Scaffold(topBar = { TopAppBar(title = { Text("Calls") }) }) { padding ->
    Text("No recent calls.", modifier = Modifier.padding(padding))
  }
}
