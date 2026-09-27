package com.example.ui.recents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.Recording
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallDetailDialog(
    call: CallRecord,
    recording: Recording?,
    onDismiss: () -> Unit,
    onCallClick: (String) -> Unit,
    onDeleteCall: (CallRecord) -> Unit,
    onPlayRecording: (Recording) -> Unit,
    onShareRecording: (Recording) -> Unit
) {
    val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy • h:mm a", Locale.getDefault())
        .format(Date(call.timestamp))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ContactAvatar(
                    name = call.contactName ?: call.phoneNumber,
                    size = 64.dp,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = call.contactName ?: "Unknown Caller",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = call.phoneNumber,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Call stats card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (call.callType) {
                                CallType.INCOMING -> "Incoming Call"
                                CallType.OUTGOING -> "Outgoing Call"
                                CallType.MISSED -> "Missed Call"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = if (call.callType == CallType.MISSED) IosRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (call.durationSeconds > 0) "${call.durationSeconds / 60}m ${call.durationSeconds % 60}s" else "No answer",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // If recording exists, show recording section
                if (recording != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IosBlue.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = IosBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Call Audio Recording",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = IosBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${recording.fileName} • ${recording.durationSeconds}s (${recording.fileSize / 1024} KB)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onPlayRecording(recording) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IosBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play Audio", fontSize = 13.sp)
                            }
                            IconButton(
                                onClick = { onShareRecording(recording) }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = IosBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCallClick(call.phoneNumber)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IosGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("dialog_call_back_button")
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Back")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        onDeleteCall(call)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = IosRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
