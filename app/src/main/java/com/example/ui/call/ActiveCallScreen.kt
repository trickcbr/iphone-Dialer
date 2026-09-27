package com.example.ui.call

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ActiveCallInfo
import com.example.model.CallStatus
import com.example.ui.components.ContactAvatar
import com.example.ui.components.DialKeypad
import com.example.ui.components.formatDuration
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed
import com.example.viewmodel.CallViewModel

@Composable
fun ActiveCallScreen(
    viewModel: CallViewModel,
    onAddCallContactPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val callInfo by viewModel.activeCall.collectAsStateWithLifecycle()
    val statusMsg by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showConsentDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleRecord()
        }
    }

    LaunchedEffect(statusMsg) {
        statusMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    if (callInfo == null) return

    val call = callInfo!!

    // Pulsing recording indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1E),
                        Color(0xFF0F0F12),
                        Color(0xFF000000)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("active_call_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Avatar, Contact info, Call timer / status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                // Recording status chip
                AnimatedVisibility(
                    visible = call.isRecording,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(IosRed.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("recording_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = IosRed,
                            modifier = Modifier.size(12.dp).alpha(pulseAlpha)
                        )
                        Text(
                            text = "REC AUDIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ContactAvatar(
                    name = call.contactName ?: call.phoneNumber,
                    size = 90.dp,
                    fontSize = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = call.contactName ?: call.phoneNumber,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (call.contactName != null) {
                    Text(
                        text = call.phoneNumber,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Call status / duration timer
                Text(
                    text = when (call.status) {
                        CallStatus.CONNECTING -> "Connecting..."
                        CallStatus.RINGING -> "Ringing..."
                        CallStatus.ACTIVE -> formatDuration(call.durationSeconds)
                        CallStatus.ON_HOLD -> "Call on Hold"
                        CallStatus.DISCONNECTED -> "Call Ended"
                        CallStatus.IDLE -> ""
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (call.status == CallStatus.ON_HOLD) Color(0xFFFF9F0A) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.testTag("call_duration_timer")
                )

                if (call.dtmfDigits.isNotEmpty()) {
                    Text(
                        text = "DTMF: ${call.dtmfDigits}",
                        fontSize = 14.sp,
                        color = IosBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Middle Section: 2x3 Grid OR In-call DTMF Keypad
            if (call.isKeypadVisible) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DialKeypad(
                        buttonSize = 64.dp,
                        spacing = 10.dp,
                        onDigitClick = { viewModel.sendDtmf(it.first()) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hide Keypad",
                        color = Color.White,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleKeypad() }
                            .padding(8.dp)
                    )
                }
            } else {
                CallControlsGrid(
                    call = call,
                    onMute = { viewModel.toggleMute() },
                    onKeypad = { viewModel.toggleKeypad() },
                    onSpeaker = { viewModel.toggleSpeaker() },
                    onAddCall = onAddCallContactPicker,
                    onHold = { viewModel.toggleHold() },
                    onRecord = {
                        if (!call.isRecording) {
                            showConsentDialog = true
                        } else {
                            viewModel.toggleRecord()
                        }
                    }
                )
            }

            // Bottom Section: End Call Button
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(IosRed)
                    .clickable { viewModel.endCall() }
                    .testTag("end_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp)
        )
    }

    // Call Recording Consent Dialog
    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Call Recording Notice") },
            text = {
                Text(
                    "Recording laws vary by jurisdiction. In some regions, all parties must consent to audio recording. " +
                            "This app stores audio locally on your device for your reference."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConsentDialog = false
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("I Understand & Start", color = IosBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CallControlsGrid(
    call: ActiveCallInfo,
    onMute: () -> Unit,
    onKeypad: () -> Unit,
    onSpeaker: () -> Unit,
    onAddCall: () -> Unit,
    onHold: () -> Unit,
    onRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: Mute, Keypad, Speaker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallControlButton(
                icon = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = "mute",
                isActive = call.isMuted,
                onClick = onMute,
                testTag = "call_mute_button"
            )
            CallControlButton(
                icon = Icons.Default.Dialpad,
                label = "keypad",
                isActive = call.isKeypadVisible,
                onClick = onKeypad,
                testTag = "call_keypad_button"
            )
            CallControlButton(
                icon = Icons.Default.VolumeUp,
                label = "speaker",
                isActive = call.isSpeakerOn,
                onClick = onSpeaker,
                testTag = "call_speaker_button"
            )
        }

        // Row 2: Add call, Hold, Record
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CallControlButton(
                icon = Icons.Default.PersonAdd,
                label = "add call",
                isActive = false,
                onClick = onAddCall,
                testTag = "call_add_button"
            )
            CallControlButton(
                icon = if (call.isOnHold) Icons.Default.PlayArrow else Icons.Default.Pause,
                label = if (call.isOnHold) "unhold" else "hold",
                isActive = call.isOnHold,
                onClick = onHold,
                testTag = "call_hold_button"
            )
            CallControlButton(
                icon = if (call.isRecording) Icons.Default.RadioButtonChecked else Icons.Default.FiberManualRecord,
                label = if (call.isRecording) "recording" else "record",
                isActive = call.isRecording,
                activeColor = IosRed,
                onClick = onRecord,
                testTag = "call_record_button"
            )
        }
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color = Color.White,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (isActive) activeColor else Color.White.copy(alpha = 0.15f)
    val tint = if (isActive) (if (activeColor == Color.White) Color.Black else Color.White) else Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(bg)
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
