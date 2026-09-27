package com.example.ui.recordings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.model.Recording
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.ContactAvatar
import com.example.ui.components.formatDuration
import com.example.ui.theme.IosBlue
import com.example.ui.theme.IosRed
import com.example.viewmodel.RecordingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    viewModel: RecordingsViewModel,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val playingId by viewModel.currentlyPlayingId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val currentPositionSeconds by viewModel.currentPositionSeconds.collectAsStateWithLifecycle()
    val toDelete by viewModel.recordingToDelete.collectAsStateWithLifecycle()
    val toRename by viewModel.recordingToRename.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()

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
                text = "Recordings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Storage badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${(totalStorageBytes / 1024)} KB",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Audio Recordings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recordings captured during your phone calls will appear here.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(recordings, key = { it.id }) { rec ->
                    val isCurrent = playingId == rec.id

                    RecordingItemCard(
                        recording = rec,
                        isCurrentPlaying = isCurrent,
                        isPlaying = isCurrent && isPlaying,
                        playbackProgress = if (isCurrent) progress else 0f,
                        currentSeconds = if (isCurrent) currentPositionSeconds else 0L,
                        onPlayPauseClick = { viewModel.playRecording(rec) },
                        onSeek = { viewModel.seekTo(it) },
                        onRenameClick = { viewModel.promptRename(rec) },
                        onShareClick = { viewModel.shareRecording(rec) },
                        onDeleteClick = { viewModel.promptDelete(rec) }
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

    // Delete confirmation dialog
    toDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDelete() },
            title = { Text("Delete Recording?") },
            text = {
                Text("Are you sure you want to delete '${rec.fileName}'? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = IosRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename dialog
    toRename?.let { rec ->
        var newName by remember { mutableStateOf(rec.fileName.removeSuffix(".m4a")) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissRename() },
            title = { Text("Rename Recording") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("rename_recording_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmRename(newName) },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Save", color = IosBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRename() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecordingItemCard(
    recording: Recording,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    playbackProgress: Float,
    currentSeconds: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onRenameClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        .format(Date(recording.timestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayPauseClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("recording_item_${recording.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = recording.contactName ?: recording.phoneNumber,
                size = 38.dp,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recording.contactName ?: recording.phoneNumber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${recording.fileName} • ${formatDuration(recording.durationSeconds)} (${recording.fileSize / 1024} KB)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Action icons: Rename, Share, Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRenameClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = IosBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = IosRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Expanded player bar when active or clicked
        AnimatedVisibility(visible = isCurrentPlaying) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                AudioPlayerBar(
                    isPlaying = isPlaying,
                    progress = playbackProgress,
                    currentSeconds = currentSeconds,
                    totalSeconds = recording.durationSeconds,
                    onPlayPauseClick = onPlayPauseClick,
                    onSeek = onSeek
                )
            }
        }
    }
}
