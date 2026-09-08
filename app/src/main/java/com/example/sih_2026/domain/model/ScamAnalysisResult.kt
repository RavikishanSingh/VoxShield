package com.example.sih_2026.domain.model

data class ScamAnalysisResult(
    val transcript: String,
    val conversationRisk: Float, // 0 to 100
    val detectedIntents: List<String>,
    val reasons: List<String>,
    val semanticConfidence: Float
)
