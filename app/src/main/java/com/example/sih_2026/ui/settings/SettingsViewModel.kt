package com.example.sih_2026.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val voiceProtectionEnabled: Boolean = true,
    val lowRiskThreshold: Float = 0.4f,
    val medRiskThreshold: Float = 0.6f,
    val highRiskThreshold: Float = 0.85f,
    val deleteRawAudio: Boolean = true
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleVoiceProtection(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(voiceProtectionEnabled = enabled)
    }

    fun toggleDeleteRawAudio(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(deleteRawAudio = enabled)
    }
}
