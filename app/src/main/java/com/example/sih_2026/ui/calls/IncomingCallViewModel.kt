package com.example.sih_2026.ui.calls

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IncomingCallUiState(
    val callerNumber: String = "+91 98765 43210",
    val isAnalyzing: Boolean = true,
    val syntheticVoiceProb: Float = 0.84f,
    val speakerMatchProb: Float = 0.62f,
    val replayProb: Float = 0.15f,
    val riskScore: Int = 78,
    val riskLevel: String = "HIGH",
    val threatWarning: String = "Possible impersonation detected"
)

class IncomingCallViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(IncomingCallUiState())
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()
}
