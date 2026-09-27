package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recordings",
    indices = [
        Index(value = ["phoneNumber"], unique = false),
        Index(value = ["timestamp"], unique = false),
        Index(value = ["callId"], unique = false)
    ]
)
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val callId: Long? = null,
    val phoneNumber: String,
    val contactName: String? = null,
    val filePath: String,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val fileSize: Long = 0,
    val note: String = ""
)
