package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

@Entity(
    tableName = "call_history",
    indices = [
        Index(value = ["phoneNumber"], unique = false),
        Index(value = ["timestamp"], unique = false)
    ]
)
data class CallRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val callType: CallType,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val hasRecording: Boolean = false,
    val recordingId: Long? = null,
    val simSlot: Int = 0
)
