package com.example.ui.recents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CallRecord
import com.example.model.CallType
import com.example.model.Recording
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosRed
import com.example.viewmodel.RecentsFilter
import com.example.viewmodel.RecentsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    viewModel: RecentsViewModel,
    onCallClick: (String) -> Unit,
    onPlayRecording: (Recording) -> Unit,
    onShareRecording: (Recording) -> Unit,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.filteredCalls.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCall by viewModel.selectedCall.collectAsStateWithLifecycle()
    val selectedRecording by viewModel.selectedCallRecording.collectAsStateWithLifecycle()

    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recents",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (calls.isNotEmpty()) {
                Text(
                    text = "Clear",
                    fontSize = 16.sp,
                    color = IosRed,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showClearConfirmation = true }
                        .padding(8.dp)
                        .testTag("clear_recents_button")
                )
            }
        }

        // iOS Style Segmented Control (All / Missed)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp)
            ) {
                SegmentButton(
                    text = "All",
                    isSelected = filter == RecentsFilter.ALL,
                    onClick = { viewModel.setFilter(RecentsFilter.ALL) },
                    modifier = Modifier.testTag("filter_all_button")
                )
                SegmentButton(
                    text = "Missed",
                    isSelected = filter == RecentsFilter.MISSED,
                    onClick = { viewModel.setFilter(RecentsFilter.MISSED) },
                    modifier = Modifier.testTag("filter_missed_button")
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by name or number", fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_recents_input")
        )

        // Calls list or empty state
        if (calls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No matching calls found" else "No Recent Calls",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(calls, key = { it.id }) { call ->
                    RecentCallItem(
                        call = call,
                        onClick = { onCallClick(call.phoneNumber) },
                        onInfoClick = { viewModel.selectCall(call) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                }
            }
        }
    }

    // Call detail dialog
    selectedCall?.let { call ->
        CallDetailDialog(
            call = call,
            recording = selectedRecording,
            onDismiss = { viewModel.selectCall(null) },
            onCallClick = onCallClick,
            onDeleteCall = { viewModel.deleteCall(it) },
            onPlayRecording = onPlayRecording,
            onShareRecording = onShareRecording
        )
    }

    // Clear confirmation dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All Recents?") },
            text = { Text("This will permanently delete your call history logs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmation = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = IosRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SegmentButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun RecentCallItem(
    call: CallRecord,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMissed = call.callType == CallType.MISSED
    val titleColor = if (isMissed) IosRed else MaterialTheme.colorScheme.onSurface
    val timeStr = formatCallTime(call.timestamp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("recent_call_item_${call.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Call type icon
        Icon(
            imageVector = when (call.callType) {
                CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
            },
            contentDescription = null,
            tint = when (call.callType) {
                CallType.INCOMING -> IosBlue
                CallType.OUTGOING -> IosGreen
                CallType.MISSED -> IosRed
            },
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Name / Number and details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = call.contactName ?: call.phoneNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (call.contactName != null) call.phoneNumber else "Mobile",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Recording chip indicator
                if (call.hasRecording) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(IosBlue.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Recorded",
                                tint = IosBlue,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "REC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosBlue
                            )
                        }
                    }
                }
            }
        }

        // Timestamp
        Text(
            text = timeStr,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Info button
        IconButton(
            onClick = onInfoClick,
            modifier = Modifier.size(32.dp).testTag("call_info_button_${call.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Call Info",
                tint = IosBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatCallTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 24 * 3600 * 1000L

    return when {
        diff < oneDay -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        diff < 2 * oneDay -> "Yesterday"
        diff < 7 * oneDay -> SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("M/d/yy", Locale.getDefault()).format(Date(timestamp))
    }
}
