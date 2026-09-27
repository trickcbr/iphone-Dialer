package com.example.telecom

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMillis: Long = 0
    private var isRecording = false

    fun startRecording(phoneNumber: String): File? {
        if (isRecording) {
            stopRecording()
        }

        try {
            val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val sanitizedNumber = phoneNumber.replace(Regex("[^0-9+]"), "").ifEmpty { "unknown" }
            val fileName = "REC_${sanitizedNumber}_$timeStamp.m4a"
            val file = File(recordingsDir, fileName)

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            // On modern Android (API 29+), VOICE_CALL requires system-level signature.
            // MIC / VOICE_COMMUNICATION is standard and compliant with Android privacy rules.
            try {
                recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            } catch (e: Exception) {
                Log.w("AudioRecorder", "VOICE_COMMUNICATION not available, falling back to MIC", e)
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            }

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioEncodingBitRate(128000)
            recorder.setAudioSamplingRate(44100)
            recorder.setOutputFile(file.absolutePath)

            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            currentOutputFile = file
            startTimeMillis = System.currentTimeMillis()
            isRecording = true
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start audio recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
            return null
        }
    }

    fun stopRecording(): RecordingResult? {
        if (!isRecording) return null
        val duration = ((System.currentTimeMillis() - startTimeMillis) / 1000).coerceAtLeast(1)
        val file = currentOutputFile

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w("AudioRecorder", "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false
        }

        return if (file != null && file.exists() && file.length() > 0) {
            RecordingResult(
                file = file,
                durationSeconds = duration,
                fileSize = file.length()
            )
        } else {
            null
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    data class RecordingResult(
        val file: File,
        val durationSeconds: Long,
        val fileSize: Long
    )
}
