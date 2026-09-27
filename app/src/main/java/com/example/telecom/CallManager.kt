package com.example.telecom

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.util.Log
import com.example.model.ActiveCallInfo
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.repository.CallHistoryRepository
import com.example.repository.RecordingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CallManager(
    private val context: Context,
    private val callHistoryRepository: CallHistoryRepository,
    private val recordingRepository: RecordingRepository
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioRecorderHelper = AudioRecorderHelper(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    private var telecomCall: Call? = null
    private var toneGenerator: ToneGenerator? = null

    private val _activeCall = MutableStateFlow<ActiveCallInfo?>(null)
    val activeCall: StateFlow<ActiveCallInfo?> = _activeCall.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
        } catch (_: Exception) {}
    }

    fun playDtmfTone(char: Char) {
        val tone = when (char) {
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '0' -> ToneGenerator.TONE_DTMF_0
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> null
        }
        if (tone != null) {
            try {
                toneGenerator?.startTone(tone, 120)
            } catch (_: Exception) {}
        }
    }

    fun startOutgoingCall(phoneNumber: String, contactName: String?) {
        telecomCall = null
        val info = ActiveCallInfo(
            phoneNumber = phoneNumber,
            contactName = contactName,
            status = CallStatus.CONNECTING,
            durationSeconds = 0
        )
        _activeCall.value = info

        // Simulate connection progression
        coroutineScope.launch {
            delay(1200)
            _activeCall.value?.let { current ->
                if (current.status == CallStatus.CONNECTING) {
                    _activeCall.value = current.copy(status = CallStatus.RINGING)
                }
            }
            delay(2000)
            _activeCall.value?.let { current ->
                if (current.status == CallStatus.RINGING || current.status == CallStatus.CONNECTING) {
                    _activeCall.value = current.copy(status = CallStatus.ACTIVE)
                    startCallTimer()
                }
            }
        }
    }

    fun setTelecomCall(
        call: Call,
        phoneNumber: String,
        contactName: String?,
        isIncoming: Boolean
    ) {
        telecomCall = call
        val initialStatus = if (isIncoming) CallStatus.RINGING else CallStatus.CONNECTING
        _activeCall.value = ActiveCallInfo(
            phoneNumber = phoneNumber,
            contactName = contactName,
            status = initialStatus
        )

        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(c: Call?, state: Int) {
                super.onStateChanged(c, state)
                when (state) {
                    Call.STATE_ACTIVE -> {
                        _activeCall.value = _activeCall.value?.copy(status = CallStatus.ACTIVE)
                        startCallTimer()
                    }
                    Call.STATE_HOLDING -> {
                        _activeCall.value = _activeCall.value?.copy(status = CallStatus.ON_HOLD, isOnHold = true)
                    }
                    Call.STATE_DISCONNECTED -> {
                        endCall()
                    }
                }
            }
        })
    }

    fun onTelecomCallRemoved(call: Call) {
        if (telecomCall == call) {
            telecomCall = null
            endCall()
        }
    }

    private fun startCallTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            while (isActive) {
                delay(1000)
                _activeCall.value?.let { current ->
                    if (current.status == CallStatus.ACTIVE) {
                        _activeCall.value = current.copy(durationSeconds = current.durationSeconds + 1)
                    }
                }
            }
        }
    }

    fun toggleMute() {
        val current = _activeCall.value ?: return
        val newMute = !current.isMuted
        try {
            audioManager.isMicrophoneMute = newMute
        } catch (_: Exception) {}
        _activeCall.value = current.copy(isMuted = newMute)
    }

    fun toggleSpeaker() {
        val current = _activeCall.value ?: return
        val newSpeaker = !current.isSpeakerOn
        try {
            audioManager.isSpeakerphoneOn = newSpeaker
        } catch (_: Exception) {}
        _activeCall.value = current.copy(isSpeakerOn = newSpeaker)
    }

    fun toggleHold() {
        val current = _activeCall.value ?: return
        val newHold = !current.isOnHold
        if (newHold) {
            telecomCall?.hold()
            _activeCall.value = current.copy(isOnHold = true, status = CallStatus.ON_HOLD)
        } else {
            telecomCall?.unhold()
            _activeCall.value = current.copy(isOnHold = false, status = CallStatus.ACTIVE)
        }
    }

    fun toggleRecord() {
        val current = _activeCall.value ?: return
        if (current.isRecording) {
            val recResult = audioRecorderHelper.stopRecording()
            _activeCall.value = current.copy(isRecording = false)
            _statusMessage.value = "Recording saved: ${recResult?.file?.name ?: ""}"
        } else {
            val file = audioRecorderHelper.startRecording(current.phoneNumber)
            if (file != null) {
                _activeCall.value = current.copy(isRecording = true)
                _statusMessage.value = "Recording started"
            } else {
                _statusMessage.value = "Audio recording not available on this device"
            }
        }
    }

    fun toggleInCallKeypad() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(isKeypadVisible = !current.isKeypadVisible)
    }

    fun sendDtmf(digit: Char) {
        playDtmfTone(digit)
        telecomCall?.playDtmfTone(digit)
        Handler(Looper.getMainLooper()).postDelayed({
            telecomCall?.stopDtmfTone()
        }, 150)
        _activeCall.value?.let { current ->
            _activeCall.value = current.copy(dtmfDigits = current.dtmfDigits + digit)
        }
    }

    fun endCall() {
        val call = _activeCall.value ?: return
        timerJob?.cancel()
        timerJob = null

        // Stop any active recording
        var recResult: AudioRecorderHelper.RecordingResult? = null
        if (audioRecorderHelper.isCurrentlyRecording() || call.isRecording) {
            recResult = audioRecorderHelper.stopRecording()
        }

        // Telecom disconnect
        try {
            telecomCall?.disconnect()
        } catch (_: Exception) {}
        telecomCall = null

        // Restore audio settings
        try {
            audioManager.isMicrophoneMute = false
            audioManager.isSpeakerphoneOn = false
        } catch (_: Exception) {}

        _activeCall.value = call.copy(status = CallStatus.DISCONNECTED)

        // Save call & recording to database
        coroutineScope.launch(Dispatchers.IO) {
            val callId = callHistoryRepository.addCallRecord(
                phoneNumber = call.phoneNumber,
                contactName = call.contactName,
                callType = CallType.OUTGOING,
                durationSeconds = call.durationSeconds,
                hasRecording = recResult != null
            )

            if (recResult != null) {
                val recId = recordingRepository.saveRecording(
                    callId = callId,
                    phoneNumber = call.phoneNumber,
                    contactName = call.contactName,
                    filePath = recResult.file.absolutePath,
                    fileName = recResult.file.name,
                    durationSeconds = recResult.durationSeconds,
                    fileSize = recResult.fileSize
                )
                callHistoryRepository.linkRecordingToCall(callId, recId)
            }
        }

        // Clear active call state after a brief moment so the user sees "Call Ended"
        coroutineScope.launch {
            delay(1200)
            _activeCall.value = null
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    companion object {
        @Volatile
        private var instance: CallManager? = null

        fun getInstance(
            context: Context,
            callHistoryRepository: CallHistoryRepository,
            recordingRepository: RecordingRepository
        ): CallManager {
            return instance ?: synchronized(this) {
                val newInstance = CallManager(
                    context.applicationContext,
                    callHistoryRepository,
                    recordingRepository
                )
                instance = newInstance
                newInstance
            }
        }
    }
}
