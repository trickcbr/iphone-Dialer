package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Recording
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    @Query("SELECT * FROM recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Long): Recording?

    @Query("SELECT * FROM recordings WHERE callId = :callId LIMIT 1")
    fun getRecordingForCall(callId: Long): Flow<Recording?>

    @Query("SELECT * FROM recordings WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getRecordingsForNumber(phoneNumber: String): Flow<List<Recording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording): Long

    @Update
    suspend fun updateRecording(recording: Recording)

    @Delete
    suspend fun deleteRecording(recording: Recording)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE recordings SET fileName = :newName WHERE id = :id")
    suspend fun updateFileName(id: Long, newName: String)

    @Query("SELECT SUM(fileSize) FROM recordings")
    suspend fun getTotalStorageUsed(): Long?

    @Query("SELECT COUNT(*) FROM recordings")
    suspend fun getRecordingCount(): Int
}
