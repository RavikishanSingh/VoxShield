package com.example.sih_2026.ui.incidents

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IncidentDetails(
    val id: String = "INC-4921",
    val severity: String = "CRITICAL",
    val caller: String = "+91 98765 43210",
    val riskScore: Int = 94,
    val threatType: String = "Voice impersonation",
    val evidence: List<String> = listOf("Synthetic voice", "Speaker mismatch", "Unknown caller", "Financial request", "High urgency"),
    val action: String = "TRANSACTION BLOCKED",
    val timestamp: String = "2026-09-07 14:32:10"
)

class IncidentViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(listOf(IncidentDetails()))
    val uiState: StateFlow<List<IncidentDetails>> = _uiState.asStateFlow()
}
