package com.example.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.database.RecordingDao
import com.example.model.Recording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RecordingRepository(
    private val recordingDao: RecordingDao,
    private val context: Context
) {
    val allRecordings: Flow<List<Recording>> = recordingDao.getAllRecordings()

    fun getRecordingForCall(callId: Long): Flow<Recording?> = recordingDao.getRecordingForCall(callId)

    suspend fun getRecordingById(id: Long): Recording? = recordingDao.getRecordingById(id)

    suspend fun saveRecording(
        callId: Long?,
        phoneNumber: String,
        contactName: String?,
        filePath: String,
        fileName: String,
        durationSeconds: Long,
        fileSize: Long
    ): Long {
        val recording = Recording(
            callId = callId,
            phoneNumber = phoneNumber,
            contactName = contactName,
            filePath = filePath,
            fileName = fileName,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            fileSize = fileSize
        )
        return recordingDao.insertRecording(recording)
    }

    suspend fun renameRecording(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val existing = recordingDao.getRecordingById(id)
        if (existing != null) {
            val oldFile = File(existing.filePath)
            val parent = oldFile.parentFile
            val extension = oldFile.extension.ifEmpty { "m4a" }
            val sanitized = newName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val targetName = if (sanitized.endsWith(".$extension")) sanitized else "$sanitized.$extension"
            val newFile = File(parent, targetName)

            if (oldFile.exists() && oldFile.renameTo(newFile)) {
                val updated = existing.copy(
                    fileName = targetName,
                    filePath = newFile.absolutePath
                )
                recordingDao.updateRecording(updated)
            } else {
                recordingDao.updateFileName(id, targetName)
            }
        }
    }

    suspend fun deleteRecording(recording: Recording) = withContext(Dispatchers.IO) {
        try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        recordingDao.deleteRecording(recording)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        val recording = recordingDao.getRecordingById(id)
        if (recording != null) {
            deleteRecording(recording)
        }
    }

    suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        recordingDao.getTotalStorageUsed() ?: 0L
    }

    fun getShareableUri(recording: Recording): Uri? {
        val file = File(recording.filePath)
        if (!file.exists()) return null
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun ensureDefaultRecordings() = withContext(Dispatchers.IO) {
        if (recordingDao.getRecordingCount() == 0) {
            val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
            val now = System.currentTimeMillis()

            val dummyFile1 = File(recordingsDir, "call_alexander_wright.m4a")
            val dummyFile2 = File(recordingsDir, "call_david_miller.m4a")

            if (!dummyFile1.exists()) {
                FileOutputStream(dummyFile1).use { it.write(ByteArray(24 * 1024)) }
            }
            if (!dummyFile2.exists()) {
                FileOutputStream(dummyFile2).use { it.write(ByteArray(48 * 1024)) }
            }

            val samples = listOf(
                Recording(
                    id = 1L,
                    callId = 1L,
                    phoneNumber = "+1 (555) 234-5678",
                    contactName = "Alexander Wright",
                    filePath = dummyFile1.absolutePath,
                    fileName = "call_alexander_wright.m4a",
                    timestamp = now - (20 * 60_000L),
                    durationSeconds = 142,
                    fileSize = dummyFile1.length().coerceAtLeast(24576L)
                ),
                Recording(
                    id = 2L,
                    callId = 4L,
                    phoneNumber = "+1 (555) 678-9012",
                    contactName = "David Miller",
                    filePath = dummyFile2.absolutePath,
                    fileName = "call_david_miller.m4a",
                    timestamp = now - (24 * 3600_000L),
                    durationSeconds = 310,
                    fileSize = dummyFile2.length().coerceAtLeast(49152L)
                )
            )
            samples.forEach { recordingDao.insertRecording(it) }
        }
    }
}
