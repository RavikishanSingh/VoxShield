package com.example.sih_2026.domain.model

data class RiskAssessment(
    val finalScore: Int,
    val riskLevel: RiskLevel,
    val title: String,
    val description: String,
    val reasons: List<String>,
    val isTemporalAlert: Boolean = false
)

enum class RiskLevel {
    LOW, SUSPICIOUS, HIGH, CRITICAL
}
