package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Recording
import com.example.repository.RecordingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class RecordingsViewModel(
    private val recordingRepository: RecordingRepository,
    private val context: Context
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    val recordings: StateFlow<List<Recording>> = recordingRepository.allRecordings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentlyPlayingId = MutableStateFlow<Long?>(null)
    val currentlyPlayingId: StateFlow<Long?> = _currentlyPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    private val _currentPositionSeconds = MutableStateFlow(0L)
    val currentPositionSeconds: StateFlow<Long> = _currentPositionSeconds.asStateFlow()

    private val _recordingToDelete = MutableStateFlow<Recording?>(null)
    val recordingToDelete: StateFlow<Recording?> = _recordingToDelete.asStateFlow()

    private val _recordingToRename = MutableStateFlow<Recording?>(null)
    val recordingToRename: StateFlow<Recording?> = _recordingToRename.asStateFlow()

    private val _totalStorageBytes = MutableStateFlow(0L)
    val totalStorageBytes: StateFlow<Long> = _totalStorageBytes.asStateFlow()

    init {
        viewModelScope.launch {
            recordingRepository.ensureDefaultRecordings()
            updateStorageUsage()
        }
    }

    fun playRecording(recording: Recording) {
        if (_currentlyPlayingId.value == recording.id && _isPlaying.value) {
            pausePlayback()
            return
        }

        if (_currentlyPlayingId.value == recording.id && mediaPlayer != null) {
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTracker()
            return
        }

        stopPlayback()

        val file = File(recording.filePath)
        if (!file.exists() || file.length() == 0L) {
            Toast.makeText(context, "Audio file is not available on disk", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                prepare()
                start()
            }
            mediaPlayer = player
            _currentlyPlayingId.value = recording.id
            _isPlaying.value = true
            startProgressTracker()

            player.setOnCompletionListener {
                stopPlayback()
            }
            player.setOnErrorListener { _, _, _ ->
                stopPlayback()
                true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to play audio: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            stopPlayback()
        }
    }

    fun pausePlayback() {
        mediaPlayer?.pause()
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun stopPlayback() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _currentlyPlayingId.value = null
        _isPlaying.value = false
        _playbackProgress.value = 0f
        _currentPositionSeconds.value = 0L
    }

    fun seekTo(fraction: Float) {
        val player = mediaPlayer ?: return
        try {
            val targetMs = (fraction * player.duration).toInt()
            player.seekTo(targetMs)
            _playbackProgress.value = fraction
            _currentPositionSeconds.value = (targetMs / 1000).toLong()
        } catch (_: Exception) {}
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying && player.duration > 0) {
                        val currentMs = player.currentPosition
                        val totalMs = player.duration
                        _playbackProgress.value = currentMs.toFloat() / totalMs
                        _currentPositionSeconds.value = (currentMs / 1000).toLong()
                    }
                }
                delay(200)
            }
        }
    }

    fun promptDelete(recording: Recording) {
        _recordingToDelete.value = recording
    }

    fun dismissDelete() {
        _recordingToDelete.value = null
    }

    fun confirmDelete() {
        val rec = _recordingToDelete.value ?: return
        if (_currentlyPlayingId.value == rec.id) {
            stopPlayback()
        }
        viewModelScope.launch {
            recordingRepository.deleteRecording(rec)
            _recordingToDelete.value = null
            updateStorageUsage()
        }
    }

    fun promptRename(recording: Recording) {
        _recordingToRename.value = recording
    }

    fun dismissRename() {
        _recordingToRename.value = null
    }

    fun confirmRename(newName: String) {
        val rec = _recordingToRename.value ?: return
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                recordingRepository.renameRecording(rec.id, newName.trim())
                _recordingToRename.value = null
            }
        }
    }

    fun shareRecording(recording: Recording) {
        val uri = recordingRepository.getShareableUri(recording)
        if (uri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Call Recording").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } else {
            Toast.makeText(context, "Audio file is not accessible", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun updateStorageUsage() {
        _totalStorageBytes.value = recordingRepository.getTotalStorageUsed()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}
