package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.ActiveCallInfo
import com.example.telecom.CallManager
import kotlinx.coroutines.flow.StateFlow

class CallViewModel(
    private val callManager: CallManager
) : ViewModel() {

    val activeCall: StateFlow<ActiveCallInfo?> = callManager.activeCall
    val statusMessage: StateFlow<String?> = callManager.statusMessage

    fun toggleMute() = callManager.toggleMute()
    fun toggleSpeaker() = callManager.toggleSpeaker()
    fun toggleHold() = callManager.toggleHold()
    fun toggleRecord() = callManager.toggleRecord()
    fun toggleKeypad() = callManager.toggleInCallKeypad()
    fun sendDtmf(digit: Char) = callManager.sendDtmf(digit)
    fun endCall() = callManager.endCall()
    fun clearStatusMessage() = callManager.clearStatusMessage()
}
