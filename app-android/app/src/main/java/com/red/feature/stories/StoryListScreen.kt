package com.red.feature.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Placeholder Stories list. Tapping the FAB would open [CameraCaptureScreen]. */
@Composable
fun StoryListScreen() {
  Scaffold(
    topBar = { TopAppBar(title = { Text("Status") }) },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { /* open CameraCaptureScreen */ },
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Add Status") }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier.size(96.dp).background(Color(0xFF1F1F1F), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text("No stories yet", color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}
