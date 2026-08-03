package com.red

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.red.feature.profile.SettingsScreen
import com.red.feature.pstn.DialPadScreen
import com.red.feature.stories.StoryListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      REDTheme { RootGraph() }
    }
  }
}

/**
 * Decides between the authentication flow and the main dashboard based on the live auth state.
 */
@Composable
private fun RootGraph() {
  val authViewModel: AuthViewModel = hiltViewModel()
  val state by authViewModel.uiState.collectAsStateWithLifecycle()

  when (state) {
    AuthUiState.Authenticated -> MainScreen()
    AuthUiState.Loading -> Unit // root stays blank briefly while persisted state loads
    else -> AuthFlow()
  }
}

@Composable
private fun MainScreen() {
  val navController = rememberNavController()
  val items = listOf(Screen.Chats, Screen.Stories, Screen.Calls, Screen.Phone, Screen.Settings)

  Scaffold(
    bottomBar = {
      NavigationBar {
        val entry by navController.currentBackStackEntryAsState()
        val current = entry?.destination
        items.forEach { screen ->
          NavigationBarItem(
            selected = current?.hierarchy?.any { it.route == screen.route } == true,
            onClick = {
              navController.navigate(screen.route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            icon = { Icon(screen.icon, contentDescription = null) },
            label = { Text(screen.label) }
          )
        }
      }
    }
  ) { padding ->
    NavHost(navController, startDestination = Screen.Chats.route, Modifier.padding(padding)) {
      composable(Screen.Chats.route) { ChatListScreen(navController) }
      composable(Screen.Stories.route) { StoryListScreen() }
      composable(Screen.Calls.route) { CallLogScreen() }
      composable(Screen.Phone.route) { DialPadScreen(onDial = { navController.navigate("pstn_call/$it") }) }
      composable(Screen.Settings.route) { SettingsScreen() }

      composable("chat_detail/{chatId}") { entry ->
        val chatId = entry.arguments?.getString("chatId") ?: ""
        com.red.feature.chat.ChatDetailScreen(conversationId = chatId)
      }
      composable("pstn_call/{number}") { entry ->
        val number = entry.arguments?.getString("number") ?: ""
        com.red.feature.pstn.PstnCallScreen(
          phoneNumber = number,
          onCallEnded = { navController.popBackStack() }
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
  data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun REDTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = darkColorScheme(
      primary = Color(0xFF2196F3),
      secondary = Color(0xFF03DAC6),
      background = Color(0xFF121212)
    ),
    content = content
  )
}
