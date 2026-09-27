package com.example.telecom

import android.net.Uri
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.example.database.AppDatabase
import com.example.repository.CallHistoryRepository
import com.example.repository.RecordingRepository

class AppInCallService : InCallService() {

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("AppInCallService", "Telecom Call added: $call")

        val database = AppDatabase.getDatabase(applicationContext)
        val historyRepo = CallHistoryRepository(database.callLogDao())
        val recRepo = RecordingRepository(database.recordingDao(), applicationContext)
        val callManager = CallManager.getInstance(applicationContext, historyRepo, recRepo)

        val handle: Uri? = call.details?.handle
        val phoneNumber = handle?.schemeSpecificPart ?: "Unknown"
        val isIncoming = call.state == Call.STATE_RINGING

        callManager.setTelecomCall(
            call = call,
            phoneNumber = phoneNumber,
            contactName = null,
            isIncoming = isIncoming
        )
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("AppInCallService", "Telecom Call removed: $call")

        val database = AppDatabase.getDatabase(applicationContext)
        val historyRepo = CallHistoryRepository(database.callLogDao())
        val recRepo = RecordingRepository(database.recordingDao(), applicationContext)
        val callManager = CallManager.getInstance(applicationContext, historyRepo, recRepo)
        callManager.onTelecomCallRemoved(call)
    }
}
