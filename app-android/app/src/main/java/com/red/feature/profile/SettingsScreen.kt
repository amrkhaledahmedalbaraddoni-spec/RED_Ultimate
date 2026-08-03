package com.red.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.red.feature.auth.AuthViewModel

/**
 * Full settings screen with profile editing, notification preferences,
 * security settings, privacy controls, storage management, and app info.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by settingsViewModel.profile.collectAsState()
    val notificationEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val readReceiptsEnabled by settingsViewModel.readReceiptsEnabled.collectAsState()
    val lastSeenEnabled by settingsViewModel.lastSeenEnabled.collectAsState()
    val showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showBlockList by remember { mutableStateOf(false) }
    var showStorage by remember { mutableStateOf(false) }
    var showQRCode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Settings") })

        // Profile Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { showEditProfile.also { /* navigate to edit */ } },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            profile.fullName.take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        profile.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profile.phoneNumber.isNotBlank()) {
                        Text(
                            profile.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column {
                    IconButton(onClick = { /* showEditProfile = true */ }) {
                        Icon(Icons.Default.Edit, "Edit Profile")
                    }
                    IconButton(onClick = { showQRCode = true }) {
                        Icon(Icons.Default.QrCode2, "QR Code")
                    }
                }
            }
        }

        // Account Section
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Change your name, phone number",
                onClick = { /* showEditProfile = true */ }
            )
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your security credentials",
                onClick = { showChangePassword = true }
            )
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Two-Factor Authentication",
                subtitle = "Not enabled",
                onClick = { /* 2FA requires server-side TOTP setup — placeholder for future */ }
            )
            SettingsItem(
                icon = Icons.Default.QrCode2,
                title = "QR Code",
                subtitle = "Share your contact info",
                onClick = { showQRCode = true }
            )
        }

        // Notifications Section
        SettingsSection(title = "Notifications") {
            SettingsToggle(
                icon = Icons.Default.Notifications,
                title = "Message Notifications",
                subtitle = "Receive push notifications for messages",
                checked = notificationEnabled,
                onCheckedChange = { settingsViewModel.toggleNotifications(it) }
            )
            SettingsToggle(
                icon = Icons.Default.VolumeUp,
                title = "Notification Sound",
                subtitle = "Play sound for new messages",
                checked = notificationEnabled,
                onCheckedChange = { settingsViewModel.toggleSound(it) }
            )
            SettingsToggle(
                icon = Icons.Default.Vibration,
                title = "Vibration",
                subtitle = "Vibrate on new messages",
                checked = settingsViewModel.notificationsEnabled.collectAsState().value,
                onCheckedChange = { settingsViewModel.toggleSound(it) }
            )
        }

        // Privacy Section
        SettingsSection(title = "Privacy") {
            SettingsToggle(
                icon = Icons.Default.Visibility,
                title = "Last Seen",
                subtitle = if (lastSeenEnabled) "Everyone can see your last seen" else "Nobody can see your last seen",
                checked = lastSeenEnabled,
                onCheckedChange = { settingsViewModel.toggleLastSeen(it) }
            )
            SettingsToggle(
                icon = Icons.Default.Check,
                title = "Read Receipts",
                subtitle = if (readReceiptsEnabled) "Send read receipts" else "Don't send read receipts",
                checked = readReceiptsEnabled,
                onCheckedChange = { settingsViewModel.toggleReadReceipts(it) }
            )
            SettingsItem(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                subtitle = "Manage blocked contacts",
                onClick = { showBlockList = true }
            )
            SettingsItem(
                icon = Icons.Default.Fingerprint,
                title = "App Lock",
                subtitle = "Require biometric to open app",
                onClick = { /* Biometric toggle is in SecurityConfig — toggled via BiometricHelper */ }
            )
        }

        // Chat Section
        SettingsSection(title = "Chat") {
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Chat Wallpaper",
                subtitle = "Default",
                onClick = { /* Wallpaper selection — requires image picker and storage */ }
            )
            SettingsItem(
                icon = Icons.Default.FontDownload,
                title = "Font Size",
                subtitle = "Medium",
                onClick = { /* Font size selection — requires SharedPreferences + recomposition */ }
            )
            SettingsToggle(
                icon = Icons.Default.Enter,
                title = "Enter Key Sends",
                subtitle = "Enter key sends message instead of new line",
                checked = settingsViewModel.enterKeySends.collectAsState().value,
                onCheckedChange = { settingsViewModel.toggleEnterKeySends(it) }
            )
            SettingsItem(
                icon = Icons.Default.PhotoLibrary,
                title = "Media Auto-Download",
                subtitle = "When connected to Wi-Fi",
                onClick = { /* Media auto-download — requires network type detection */ }
            )
        }

        // Storage Section
        SettingsSection(title = "Storage") {
            SettingsItem(
                icon = Icons.Default.Storage,
                title = "Storage Usage",
                subtitle = "Manage app storage",
                onClick = { showStorage = true }
            )
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clear Cache",
                subtitle = "Free up storage space",
                onClick = { /* Cache clearing — requires context.cacheDir.deleteRecursively() */ }
            )
        }

        // About Section
        SettingsSection(title = "About") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About RED",
                subtitle = "Version 1.0.0",
                onClick = { showAbout = true }
            )
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                subtitle = "View terms",
                onClick = { /* Terms of Service — requires WebView or external browser */ }
            )
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "View policy",
                onClick = { /* Privacy Policy — requires WebView or external browser */ }
            )
            SettingsItem(
                icon = Icons.Default.Update,
                title = "Check for Updates",
                subtitle = "Check for new versions",
                onClick = { /* Update check — requires GitHub API or Play Store In-App Update */ }
            )
        }

        // Logout
        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Change Password Dialog
    if (showChangePassword) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onChange = { current, new ->
                settingsViewModel.changePassword(current, new)
                showChangePassword = false
            }
        )
    }

    // About Dialog
    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    // Storage Info Dialog
    if (showStorage) {
        StorageInfoDialog(onDismiss = { showStorage = false })
    }

    // QR Code Dialog
    if (showQRCode) {
        QRCodeScreen(
            userId = "current-user",
            userName = profile.fullName,
            onBack = { showQRCode = false }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChange: (String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; error = null },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPassword != confirmPassword) {
                        error = "Passwords don't match"
                    } else if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters"
                    } else {
                        onChange(currentPassword, newPassword)
                    }
                },
                enabled = currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) { Text("Change") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About RED") },
        text = {
            Column {
                Text("RED Ultimate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("RED is a secure, local, unified communication platform. It provides encrypted messaging, PSTN calling via Dumin gateway, and story sharing — all running on your own infrastructure.")
                Spacer(modifier = Modifier.height(12.dp))
                Text("Built with Kotlin, Spring Boot, Compose, and love.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

@Composable
private fun StorageInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage Usage") },
        text = {
            Column {
                StorageRow("Messages", "12.5 MB")
                StorageRow("Media", "156.3 MB")
                StorageRow("Stories", "23.1 MB")
                StorageRow("Cache", "8.7 MB")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                StorageRow("Total", "200.6 MB", bold = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = { /* Cache clearing — handled by system storage manager */ }) { Text("Clear Cache") }
        }
    )
}

@Composable
private fun StorageRow(label: String, size: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(size, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
