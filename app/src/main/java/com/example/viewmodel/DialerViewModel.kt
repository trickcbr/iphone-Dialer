package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Contact
import com.example.repository.ContactRepository
import com.example.repository.SettingsRepository
import com.example.telecom.CallManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DialerViewModel(
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val callManager: CallManager,
    private val context: Context
) : ViewModel() {

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    private val _matchedContacts = MutableStateFlow<List<Contact>>(emptyList())
    val matchedContacts: StateFlow<List<Contact>> = _matchedContacts.asStateFlow()

    val contacts: StateFlow<List<Contact>> = contactRepository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            contactRepository.ensureDefaultContacts()
        }
    }

    fun onDigitPressed(digit: String) {
        val newNumber = _dialedNumber.value + digit
        _dialedNumber.value = newNumber
        triggerFeedback(digit.firstOrNull())
        updateMatches(newNumber)
    }

    fun onLongPressZero() {
        val newNumber = _dialedNumber.value + "+"
        _dialedNumber.value = newNumber
        triggerFeedback('+')
        updateMatches(newNumber)
    }

    fun onBackspace() {
        if (_dialedNumber.value.isNotEmpty()) {
            val newNumber = _dialedNumber.value.dropLast(1)
            _dialedNumber.value = newNumber
            triggerFeedback(null)
            updateMatches(newNumber)
        }
    }

    fun onClearAll() {
        _dialedNumber.value = ""
        _matchedContacts.value = emptyList()
        triggerFeedback(null)
    }

    fun setNumber(number: String) {
        _dialedNumber.value = number
        updateMatches(number)
    }

    private fun updateMatches(query: String) {
        if (query.isBlank()) {
            _matchedContacts.value = emptyList()
            return
        }
        viewModelScope.launch {
            val sanitized = query.replace(Regex("[^0-9+]"), "")
            val results = contactRepository.lookupMatches(sanitized)
            _matchedContacts.value = results
        }
    }

    private fun triggerFeedback(char: Char?) {
        if (char != null && settingsRepository.dtmfTonesEnabled.value) {
            callManager.playDtmfTone(char)
        }
        if (settingsRepository.vibrationFeedback.value) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(25)
                }
            } catch (_: Exception) {}
        }
    }

    fun makeCall(phoneNumber: String? = null, contactName: String? = null) {
        val targetNumber = phoneNumber ?: _dialedNumber.value
        if (targetNumber.isBlank()) return

        // Launch in-app call screen via CallManager
        callManager.startOutgoingCall(targetNumber, contactName)

        // Also initiate real system call intent if device allows
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:${Uri.encode(targetNumber)}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // If CALL_PHONE permission not granted, fallback to ACTION_DIAL
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(targetNumber)}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {}
        }
    }
}
