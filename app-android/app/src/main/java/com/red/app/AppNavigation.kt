package com.red.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.red.feature.auth.LoginScreen
import com.red.feature.auth.PendingApprovalScreen
import com.red.feature.auth.PermissionRequestScreen
import com.red.feature.auth.RegisterScreen
import com.red.feature.auth.WelcomeScreen

/**
 * The authentication flow (permissions -> welcome -> register/login -> pending). Rendered by the
 * root graph whenever the user is not yet authenticated. On a successful login the shared
 * [com.red.feature.auth.AuthViewModel] flips to Authenticated and the root graph swaps to the main
 * dashboard automatically.
 */
@Composable
fun AuthFlow() {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "permissions") {
    composable("permissions") {
      PermissionRequestScreen(onAllPermissionsGranted = {
        navController.navigate("welcome") { popUpTo("permissions") { inclusive = true } }
      })
    }
    composable("welcome") {
      WelcomeScreen(
        onNavigateToRegister = { navController.navigate("register") },
        onNavigateToLogin = { navController.navigate("login") }
      )
    }
    composable("register") {
      RegisterScreen(onRegistrationSubmitted = { navController.navigate("pending_approval") })
    }
    composable("login") {
      LoginScreen(onLoginSuccess = { /* root graph swaps to main on Authenticated state */ })
    }
    composable("pending_approval") { PendingApprovalScreen() }
  }
}
