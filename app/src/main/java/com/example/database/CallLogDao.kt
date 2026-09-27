package com.example.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CallRecord
import com.example.model.CallType
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCallRecords(): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_history WHERE callType = :missedType ORDER BY timestamp DESC")
    fun getMissedCalls(missedType: CallType = CallType.MISSED): Flow<List<CallRecord>>

    @Query("SELECT * FROM call_history WHERE id = :id LIMIT 1")
    suspend fun getCallRecordById(id: Long): CallRecord?

    @Query("SELECT * FROM call_history WHERE phoneNumber = :number ORDER BY timestamp DESC LIMIT 10")
    fun getCallHistoryForNumber(number: String): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(record: CallRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<CallRecord>)

    @Update
    suspend fun updateCallRecord(record: CallRecord)

    @Delete
    suspend fun deleteCallRecord(record: CallRecord)

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_history")
    suspend fun clearAllHistory()

    @Query("UPDATE call_history SET hasRecording = 1, recordingId = :recordingId WHERE id = :callId")
    suspend fun linkRecording(callId: Long, recordingId: Long)

    @Query("SELECT COUNT(*) FROM call_history")
    suspend fun getCallCount(): Int
}
