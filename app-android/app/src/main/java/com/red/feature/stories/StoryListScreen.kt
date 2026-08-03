package com.red.feature.stories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoryListScreen(navController: NavController? = null) {
    val viewModel: StoryViewModel = hiltViewModel()
    val stories by viewModel.stories.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController?.navigate("story_capture") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Story")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TopAppBar(title = { Text("Stories") }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ))

            if (stories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No stories yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(stories) { story ->
                        StoryRow(story)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryRow(story: StoryDto) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Story by ${story.ownerId.take(8)}…", style = MaterialTheme.typography.titleMedium)
            Text(formatExpiry(story.expiresAt), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatExpiry(ts: Long): String {
    val remaining = ts - System.currentTimeMillis()
    return if (remaining > 0) "Expires in ${remaining / 3600000}h" else "Expired"
}
