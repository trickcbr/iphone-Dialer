package com.example.repository

import com.example.database.CallLogDao
import com.example.model.CallRecord
import com.example.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CallHistoryRepository(
    private val callLogDao: CallLogDao
) {
    val allCalls: Flow<List<CallRecord>> = callLogDao.getAllCallRecords()
    val missedCalls: Flow<List<CallRecord>> = callLogDao.getMissedCalls()

    fun getCallHistoryForNumber(number: String): Flow<List<CallRecord>> =
        callLogDao.getCallHistoryForNumber(number)

    suspend fun getCallById(id: Long): CallRecord? = callLogDao.getCallRecordById(id)

    suspend fun addCallRecord(
        phoneNumber: String,
        contactName: String?,
        callType: CallType,
        durationSeconds: Long,
        hasRecording: Boolean = false,
        recordingId: Long? = null
    ): Long {
        val record = CallRecord(
            phoneNumber = phoneNumber,
            contactName = contactName,
            callType = callType,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds,
            hasRecording = hasRecording,
            recordingId = recordingId
        )
        return callLogDao.insertCallRecord(record)
    }

    suspend fun linkRecordingToCall(callId: Long, recordingId: Long) {
        callLogDao.linkRecording(callId, recordingId)
    }

    suspend fun deleteCall(record: CallRecord) {
        callLogDao.deleteCallRecord(record)
    }

    suspend fun deleteCallById(id: Long) {
        callLogDao.deleteById(id)
    }

    suspend fun clearHistory() {
        callLogDao.clearAllHistory()
    }

    suspend fun ensureDefaultHistory() = withContext(Dispatchers.IO) {
        if (callLogDao.getCallCount() == 0) {
            val now = System.currentTimeMillis()
            val hour = 3600_000L
            val day = 24 * hour

            val sampleRecords = listOf(
                CallRecord(
                    phoneNumber = "+1 (555) 234-5678",
                    contactName = "Alexander Wright",
                    callType = CallType.INCOMING,
                    timestamp = now - (20 * 60_000L),
                    durationSeconds = 142,
                    hasRecording = true,
                    recordingId = 1L
                ),
                CallRecord(
                    phoneNumber = "+1 (555) 345-6789",
                    contactName = "Alice Morgan",
                    callType = CallType.OUTGOING,
                    timestamp = now - (2 * hour),
                    durationSeconds = 85,
                    hasRecording = false
                ),
                CallRecord(
                    phoneNumber = "+1 (555) 890-1234",
                    contactName = "Gabriel Hayes",
                    callType = CallType.MISSED,
                    timestamp = now - (5 * hour),
                    durationSeconds = 0,
                    hasRecording = false
                ),
                CallRecord(
                    phoneNumber = "+1 (555) 678-9012",
                    contactName = "David Miller",
                    callType = CallType.INCOMING,
                    timestamp = now - (1 * day),
                    durationSeconds = 310,
                    hasRecording = true,
                    recordingId = 2L
                ),
                CallRecord(
                    phoneNumber = "+1 (555) 456-7890",
                    contactName = "Benjamin Clark",
                    callType = CallType.OUTGOING,
                    timestamp = now - (2 * day),
                    durationSeconds = 47,
                    hasRecording = false
                ),
                CallRecord(
                    phoneNumber = "+1 (555) 901-2345",
                    contactName = "Hannah Abbott",
                    callType = CallType.MISSED,
                    timestamp = now - (3 * day),
                    durationSeconds = 0,
                    hasRecording = false
                )
            )
            callLogDao.insertAll(sampleRecords)
        }
    }
}
