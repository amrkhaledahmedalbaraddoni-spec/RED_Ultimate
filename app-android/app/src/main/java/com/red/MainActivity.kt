package com.red

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.red.core.delivery.ConnectionStatusBanner
import com.red.core.security.BiometricHelper
import com.red.feature.auth.AppLockScreen
import com.red.feature.auth.AuthUiState
import com.red.feature.auth.AuthViewModel
import com.red.feature.calls.CallLogScreen
import com.red.feature.calls.VideoCallScreen
import com.red.feature.chat.ChatListScreen
import com.red.feature.chat.CreateGroupScreen
import com.red.feature.chat.ForwardMessageScreen
import com.red.feature.chat.GroupDetailScreen
import com.red.feature.chat.MessageSearchScreen
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

  // Check if app lock is enabled
  val context = androidx.compose.ui.platform.LocalContext.current
  val isAppLockEnabled = BiometricHelper.isAppLockEnabled(context)
  var isAppUnlocked by remember { mutableStateOf(!isAppLockEnabled) }

  when {
    !isAppUnlocked && isAppLockEnabled -> {
      AppLockScreen(
        onUnlock = { isAppUnlocked = true },
        onUnlockWithBiometric = { isAppUnlocked = true }
      )
    }
    state == AuthUiState.Authenticated -> MainScreen()
    state == AuthUiState.Loading -> {
      // Show loading
    }
    else -> AuthFlow()
  }
}

@Composable
private fun MainScreen() {
  val navController = rememberNavController()
  val items = listOf(
    Screen.Chats, Screen.Stories, Screen.Calls, Screen.Phone, Screen.Contacts, Screen.Settings
  )

  // Connection status
  var isConnected by remember { mutableStateOf(true) }

  Scaffold(
    bottomBar = {
      NavigationBar {
        val entry by navController.currentBackStackEntryAsState()
        val current = entry?.destination
        items.forEach { screen ->
          NavigationBarItem(
            selected = current?.hierarchy?.any { it.route == screen.route } == true,
            onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
            icon = { Icon(screen.icon, contentDescription = null) },
            label = { Text(screen.label) }
          )
        }
      }
    }
  ) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      // Connection status banner
      ConnectionStatusBanner(
        isConnected = isConnected,
        isReconnecting = false,
        onRetry = { /* Trigger reconnect */ }
      )

      NavHost(navController, startDestination = Screen.Chats.route) {
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

        // Video call
        composable("video_call/{peerName}") { entry ->
          val peerName = entry.arguments?.getString("peerName") ?: "User"
          VideoCallScreen(
            peerName = peerName,
            onEndCall = { navController.popBackStack() }
          )
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

        // Create group
        composable("create_group") {
          CreateGroupScreen(
            onBack = { navController.popBackStack() },
            onCreateGroup = { name, members -> navController.popBackStack() }
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

        // Group detail
        composable("group_detail/{groupId}") { entry ->
          val groupId = entry.arguments?.getString("groupId") ?: ""
          GroupDetailScreen(
            groupId = groupId,
            onBack = { navController.popBackStack() },
            onChatWith = { id, name -> navController.navigate("chat_detail/$id") }
          )
        }

        // Forward message
        composable("forward_message/{messageId}/{payload}") { entry ->
          val messageId = entry.arguments?.getString("messageId") ?: ""
          val payload = entry.arguments?.getString("payload") ?: ""
          ForwardMessageScreen(
            messageId = messageId,
            messagePayload = payload,
            onBack = { navController.popBackStack() },
            onForwarded = { navController.popBackStack() }
          )
        }

        // Search
        composable("search") {
          MessageSearchScreen(
            onBack = { navController.popBackStack() },
            onChatWith = { id, name -> navController.navigate("chat_detail/$id") },
            onGroupClick = { groupId -> navController.navigate("group_detail/$groupId") }
          )
        }
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
