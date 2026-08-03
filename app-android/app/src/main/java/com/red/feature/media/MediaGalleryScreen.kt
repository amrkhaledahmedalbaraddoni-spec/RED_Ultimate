package com.red.feature.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage

/**
 * Media gallery — shows all shared media (images, videos, documents)
 * in a conversation, organized by type.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(
    conversationId: String,
    onBack: () -> Unit = {},
    onMediaClick: (String) -> Unit = {},
    viewModel: MediaGalleryViewModel = hiltViewModel()
) {
    val mediaItems by viewModel.mediaItems.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(conversationId) { viewModel.load(conversationId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared Media") },
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
        ) {
            // Tab row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Images") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Videos") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Files") })
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filtered = when (selectedTab) {
                    0 -> mediaItems.filter { it.type == "IMAGE" }
                    1 -> mediaItems.filter { it.type == "VIDEO" }
                    else -> mediaItems.filter { it.type == "FILE" }
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                when (selectedTab) {
                                    0 -> Icons.Default.Image
                                    1 -> Icons.Default.Videocam
                                    else -> Icons.Default.Description
                                },
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                when (selectedTab) {
                                    0 -> "No images"
                                    1 -> "No videos"
                                    else -> "No files"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (selectedTab == 0) {
                    // Image grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filtered) { item ->
                            AsyncImage(
                                model = item.url,
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clickable { onMediaClick(item.url) },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // List for videos/files
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(filtered) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMediaClick(item.url) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    when (item.type) {
                                        "VIDEO" -> Icons.Default.Videocam
                                        else -> Icons.Default.Description
                                    },
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        item.size,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MediaItem(
    val id: String,
    val type: String,  // IMAGE, VIDEO, FILE
    val url: String,
    val name: String,
    val size: String,
    val timestamp: Long
)
