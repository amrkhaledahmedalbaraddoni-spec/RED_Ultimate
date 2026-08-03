package com.red.feature.profile

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
import com.red.core.auth.TokenStore
import com.red.core.delivery.ClientIdentity
import com.red.feature.auth.AuthViewModel

/**
 * Full settings screen with profile editing, notification preferences,
 * security settings, and app information.
 */
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val profile by settingsViewModel.profile.collectAsState()
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = { Text("Settings") })

        // Profile Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                IconButton(onClick = { showEditProfile = true }) {
                    Icon(Icons.Default.Edit, "Edit Profile")
                }
            }
        }

        // Settings Sections
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Change your name, phone number",
                onClick = { showEditProfile = true }
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
                subtitle = "Coming soon"
            )
        }

        SettingsSection(title = "Notifications") {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Message Notifications",
                subtitle = "Enabled"
            )
            SettingsItem(
                icon = Icons.Default.VolumeUp,
                title = "Notification Sound",
                subtitle = "Default"
            )
        }

        SettingsSection(title = "Privacy") {
            SettingsItem(
                icon = Icons.Default.Visibility,
                title = "Last Seen",
                subtitle = "Everyone"
            )
            SettingsItem(
                icon = Icons.Default.Check,
                title = "Read Receipts",
                subtitle = "Enabled"
            )
            SettingsItem(
                icon = Icons.Default.Block,
                title = "Blocked Users",
                subtitle = "Manage blocked contacts"
            )
        }

        SettingsSection(title = "Storage") {
            SettingsItem(
                icon = Icons.Default.Storage,
                title = "Storage Usage",
                subtitle = "Manage app storage"
            )
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Clear Cache",
                subtitle = "Free up storage space"
            )
        }

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
                subtitle = "View terms"
            )
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "View policy"
            )
        }

        // Logout
        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
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

    // Edit Profile Dialog
    if (showEditProfile) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditProfile = false },
            onSave = { name, phone ->
                settingsViewModel.updateProfile(name, phone)
                showEditProfile = false
            }
        )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
            .then(if (onClick != null) Modifier.padding(0.dp) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    profile: ProfileInfo,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile.fullName) }
    var phone by remember { mutableStateOf(profile.phoneNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, phone) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPassword != confirmPassword) {
                    error = "Passwords don't match"
                } else if (newPassword.length < 8) {
                    error = "Password must be at least 8 characters"
                } else {
                    onChange(currentPassword, newPassword)
                }
            }) { Text("Change") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("RED Sovereign") },
        text = {
            Column {
                Text("RED Ultimate v1.0.0", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("A sovereign communication platform built on a hardened Signal-Android fork with custom components:")
                Spacer(modifier = Modifier.height(4.dp))
                Text("• System A: Ultra HD VoIP (4K/AV1)")
                Text("• System B: PSTN/Dumin Gateway")
                Text("• System C: Guaranteed Delivery")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Secure. Local. Unified.", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("100% on-prem. No cloud dependencies.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}

import androidx.compose.foundation.clickable
