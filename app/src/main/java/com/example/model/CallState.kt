package com.example.model

enum class CallStatus {
    IDLE,
    CONNECTING,
    RINGING,
    ACTIVE,
    ON_HOLD,
    DISCONNECTED
}

data class ActiveCallInfo(
    val callId: String = System.currentTimeMillis().toString(),
    val phoneNumber: String,
    val contactName: String? = null,
    val status: CallStatus = CallStatus.CONNECTING,
    val durationSeconds: Long = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isOnHold: Boolean = false,
    val isRecording: Boolean = false,
    val isKeypadVisible: Boolean = false,
    val dtmfDigits: String = ""
)
