package com.example.sih_2026.domain.model

data class CallLogEntry(
    val callerName: String,
    val phoneNumber: String,
    val timestamp: String,
    val riskScore: Int,
    val riskLevel: String,
    val reasons: List<String>,
)
