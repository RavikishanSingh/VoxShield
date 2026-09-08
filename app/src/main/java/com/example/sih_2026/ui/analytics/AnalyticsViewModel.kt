package com.example.sih_2026.ui.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AnalyticsUiState(
    val totalCallsAnalyzed: Int = 438,
    val totalThreats: Int = 27,
    val deepfakeThreats: Int = 11,
    val replayThreats: Int = 6,
    val impersonationThreats: Int = 7,
    val otherThreats: Int = 3,
    val detectionRate: Int = 94
)

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
}
