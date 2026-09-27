package com.example.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.repository.AppThemeMode
import com.example.repository.SettingsRepository
import com.example.ui.components.isAppDefaultDialer
import com.example.ui.components.requestDefaultDialerRole
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosYellow

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by settingsRepository.themeMode.collectAsStateWithLifecycle()
    val autoRecord by settingsRepository.autoRecord.collectAsStateWithLifecycle()
    val dtmfTones by settingsRepository.dtmfTonesEnabled.collectAsStateWithLifecycle()
    val vibration by settingsRepository.vibrationFeedback.collectAsStateWithLifecycle()

    var isDefault by remember { mutableStateOf(isAppDefaultDialer(context)) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showRecordingInfoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Default Dialer Section
        SettingsCard(title = "DEFAULT PHONE APP") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingIconBadge(Icons.Default.Phone, IosGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Default Phone App",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDefault) "Configured as default" else "Not set as default",
                            fontSize = 12.sp,
                            color = if (isDefault) IosGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isDefault) {
                    Button(
                        onClick = {
                            requestDefaultDialerRole(context) { intent ->
                                context.startActivity(intent)
                                isDefault = isAppDefaultDialer(context)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enable", fontSize = 13.sp)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Default set",
                        tint = IosGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Appearance Section
        SettingsCard(title = "APPEARANCE") {
            SettingsRow(
                icon = Icons.Default.DarkMode,
                iconColor = IosBlue,
                title = "Theme",
                subtitle = when (currentTheme) {
                    AppThemeMode.SYSTEM -> "Follow System"
                    AppThemeMode.LIGHT -> "Light Mode"
                    AppThemeMode.DARK -> "Dark Mode"
                },
                onClick = { showThemeDialog = true }
            )
        }

        // Call Recording Section
        SettingsCard(title = "CALL RECORDING") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    SettingIconBadge(Icons.Default.Mic, IosRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Record Calls",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Automatically capture audio on connected calls",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = autoRecord,
                    onCheckedChange = { settingsRepository.setAutoRecord(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosGreen)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 56.dp)
            )

            SettingsRow(
                icon = Icons.Default.Folder,
                iconColor = IosYellow,
                title = "Storage Location",
                subtitle = "App-specific internal files/recordings",
                onClick = { showRecordingInfoDialog = true }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 56.dp)
            )

            SettingsRow(
                icon = Icons.Default.Info,
                iconColor = IosBlue,
                title = "Android Compatibility Notes",
                subtitle = "Learn about audio restrictions on Android 10+",
                onClick = { showRecordingInfoDialog = true }
            )
        }

        // Keypad Sounds & Haptics
        SettingsCard(title = "KEYPAD & FEEDBACK") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingIconBadge(Icons.Default.VolumeUp, IosBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Keypad Audio Tones",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = dtmfTones,
                    onCheckedChange = { settingsRepository.setDtmfTonesEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosGreen)
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 56.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SettingIconBadge(Icons.Default.Vibration, IosGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vibration Feedback",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = vibration,
                    onCheckedChange = { settingsRepository.setVibrationFeedback(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = IosGreen)
                )
            }
        }

        // Permissions Status Overview
        SettingsCard(title = "PERMISSIONS STATUS") {
            PermissionStatusRow("Call Phone", Manifest.permission.CALL_PHONE, context)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            PermissionStatusRow("Read Contacts", Manifest.permission.READ_CONTACTS, context)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            PermissionStatusRow("Record Audio", Manifest.permission.RECORD_AUDIO, context)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(start = 16.dp))
            PermissionStatusRow("Notifications", Manifest.permission.POST_NOTIFICATIONS, context)
        }

        // History Management
        SettingsCard(title = "DATA MANAGEMENT") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearHistoryConfirm = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clear All Call History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosRed
                )
            }
        }

        // About & Legal
        SettingsCard(title = "ABOUT") {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = "iPhone-style Dialer for Android",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Version 1.0 • Designed with Jetpack Compose & Telecom",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    settingsRepository.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (mode) {
                                    AppThemeMode.SYSTEM -> "Follow System"
                                    AppThemeMode.LIGHT -> "Light Mode"
                                    AppThemeMode.DARK -> "Dark Mode"
                                },
                                fontSize = 15.sp,
                                fontWeight = if (currentTheme == mode) FontWeight.Bold else FontWeight.Normal
                            )
                            if (currentTheme == mode) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = IosBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear history confirmation
    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirm = false },
            title = { Text("Clear All Call Logs?") },
            text = { Text("This will permanently remove all call history from your device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = IosRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Android Compatibility Info Dialog
    if (showRecordingInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRecordingInfoDialog = false },
            title = { Text("Android Call Recording Policy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Starting with Android 9 (Pie) and fully enforced in Android 10+ (API 29+), " +
                                "Google restricted access to the VOICE_CALL and uplink/downlink cellular audio streams " +
                                "for third-party apps without system privileged permissions."
                    )
                    Text(
                        "This app uses the officially supported MediaRecorder Voice Communication/Microphone source. " +
                                "When in speaker mode or on supported devices, call audio is captured with high fidelity. " +
                                "All audio files are saved securely in your private application storage.",
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRecordingInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingIconBadge(icon, iconColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingIconBadge(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PermissionStatusRow(
    name: String,
    permission: String,
    context: Context
) {
    val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = if (granted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (granted) IosGreen else IosRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (granted) "Granted" else "Not Granted",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (granted) IosGreen else IosRed
            )
        }
    }
}
