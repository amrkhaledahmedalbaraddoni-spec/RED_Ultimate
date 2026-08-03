package com.red

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.red.app.AuthFlow
import com.red.feature.auth.AuthUiState
import com.red.feature.auth.AuthViewModel
import com.red.feature.calls.CallLogScreen
import com.red.feature.chat.ChatListScreen
import com.red.feature.chat.NewChatScreen
import com.red.feature.contacts.ContactsScreen
import com.red.feature.profile.SettingsScreen
import com.red.feature.profile.ProfileScreen
import com.red.feature.profile.QRCodeScreen
import com.red.feature.block.BlockListScreen
import com.red.feature.notifications.NotificationListScreen
import com.red.feature.media.MediaGalleryScreen
import com.red.feature.pstn.DialPadScreen
import com.red.feature.stories.StoryListScreen
import com.red.feature.stories.StoryViewModel
import com.red.feature.stories.CameraCaptureScreen
import com.red.feature.stories.StoryViewerScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { REDTheme { RootGraph() } }
  }
}

@Composable
private fun RootGraph() {
  val authViewModel: AuthViewModel = hiltViewModel()
  val state by authViewModel.uiState.collectAsStateWithLifecycle()
  when (state) {
    AuthUiState.Authenticated -> MainScreen()
    AuthUiState.Loading -> Unit
    else -> AuthFlow()
  }
}

@Composable
private fun MainScreen() {
  val navController = rememberNavController()
  val items = listOf(
    Screen.Chats, Screen.Stories, Screen.Calls, Screen.Phone, Screen.Contacts, Screen.Settings
  )

  Scaffold(
    bottomBar = {
      NavigationBar {
        val entry by navController.currentBackStackEntryAsState()
        val current = entry?.destination
        items.forEach { screen ->
          NavigationBarItem(
            selected = current?.hierarchy?.any { it.route == screen.route } == true,
            onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
            icon = {
              if (screen == Screen.Chats) {
                BadgedBox(badge = { /* TODO: Unread count badge */ }) {
                  Icon(screen.icon, contentDescription = null)
                }
              } else {
                Icon(screen.icon, contentDescription = null)
              }
            },
            label = { Text(screen.label) }
          )
        }
      }
    }
  ) { padding ->
    NavHost(navController, startDestination = Screen.Chats.route, Modifier.padding(padding)) {
      composable(Screen.Chats.route) { ChatListScreen(navController) }
      composable(Screen.Stories.route) { StoryListScreen(navController) }
      composable(Screen.Calls.route) { CallLogScreen(onCallClick = { number -> navController.navigate("pstn_call/$number") }) }
      composable(Screen.Phone.route) { DialPadScreen(onDial = { navController.navigate("pstn_call/$it") }) }
      composable(Screen.Contacts.route) {
        ContactsScreen(
          onChatWith = { userId, name -> navController.navigate("chat_detail/$userId") },
          onCallUser = { number -> navController.navigate("pstn_call/$number") }
        )
      }
      composable(Screen.Settings.route) { SettingsScreen() }

      // Chat detail
      composable("chat_detail/{chatId}") { entry ->
        val chatId = entry.arguments?.getString("chatId") ?: ""
        com.red.feature.chat.ChatDetailScreen(conversationId = chatId)
      }

      // PSTN call
      composable("pstn_call/{number}") { entry ->
        val number = entry.arguments?.getString("number") ?: ""
        com.red.feature.pstn.PstnCallScreen(phoneNumber = number, onCallEnded = { navController.popBackStack() })
      }

      // Story capture
      composable("story_capture") {
        val storyVm: StoryViewModel = hiltViewModel()
        CameraCaptureScreen(onImageCaptured = { uri ->
          storyVm.publish(uri)
          navController.popBackStack()
        })
      }

      // Story viewer
      composable("story_viewer/{urls}") { entry ->
        val urls = entry.arguments?.getString("urls")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        StoryViewerScreen(stories = urls, onAllStoriesViewed = { navController.popBackStack() })
      }

      // New chat
      composable("new_chat") {
        NewChatScreen(
          onBack = { navController.popBackStack() },
          onUserSelected = { userId, name -> navController.navigate("chat_detail/$userId") }
        )
      }

      // Profile
      composable("profile/{userId}") { entry ->
        val userId = entry.arguments?.getString("userId") ?: ""
        ProfileScreen(
          userId = userId,
          onBack = { navController.popBackStack() },
          onChat = { id -> navController.navigate("chat_detail/$id") }
        )
      }

      // Block list
      composable("block_list") {
        BlockListScreen(onBack = { navController.popBackStack() })
      }

      // Notifications
      composable("notifications") {
        NotificationListScreen(onBack = { navController.popBackStack() })
      }

      // Media gallery
      composable("media_gallery/{conversationId}") { entry ->
        val conversationId = entry.arguments?.getString("conversationId") ?: ""
        MediaGalleryScreen(
          conversationId = conversationId,
          onBack = { navController.popBackStack() }
        )
      }

      // QR Code
      composable("qr_code") {
        QRCodeScreen(
          userId = "current-user",
          userName = "Me",
          onBack = { navController.popBackStack() }
        )
      }
    }
  }
}

private sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
  data object Chats : Screen("chats", "Chats", Icons.Default.Chat)
  data object Stories : Screen("stories", "Status", Icons.Default.History)
  data object Calls : Screen("calls", "Calls", Icons.Default.Call)
  data object Phone : Screen("phone", "Phone", Icons.Default.Dialpad)
  data object Contacts : Screen("contacts", "Contacts", Icons.Default.Contacts)
  data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun REDTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = darkColorScheme(
      primary = Color(0xFF2196F3),
      secondary = Color(0xFF03DAC6),
      background = Color(0xFF121212),
      surface = Color(0xFF1E1E1E),
      error = Color(0xFFCF6679)
    ),
    content = content
  )
}
