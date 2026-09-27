package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dialer_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _autoRecord = MutableStateFlow(prefs.getBoolean("auto_record", false))
    val autoRecord: StateFlow<Boolean> = _autoRecord.asStateFlow()

    private val _recordingConsentAccepted =
        MutableStateFlow(prefs.getBoolean("recording_consent", false))
    val recordingConsentAccepted: StateFlow<Boolean> = _recordingConsentAccepted.asStateFlow()

    private val _dtmfTonesEnabled = MutableStateFlow(prefs.getBoolean("dtmf_tones", true))
    val dtmfTonesEnabled: StateFlow<Boolean> = _dtmfTonesEnabled.asStateFlow()

    private val _vibrationFeedback = MutableStateFlow(prefs.getBoolean("vibrate_feedback", true))
    val vibrationFeedback: StateFlow<Boolean> = _vibrationFeedback.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun setAutoRecord(enabled: Boolean) {
        prefs.edit().putBoolean("auto_record", enabled).apply()
        _autoRecord.value = enabled
    }

    fun setRecordingConsentAccepted(accepted: Boolean) {
        prefs.edit().putBoolean("recording_consent", accepted).apply()
        _recordingConsentAccepted.value = accepted
    }

    fun setDtmfTonesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dtmf_tones", enabled).apply()
        _dtmfTonesEnabled.value = enabled
    }

    fun setVibrationFeedback(enabled: Boolean) {
        prefs.edit().putBoolean("vibrate_feedback", enabled).apply()
        _vibrationFeedback.value = enabled
    }
}
