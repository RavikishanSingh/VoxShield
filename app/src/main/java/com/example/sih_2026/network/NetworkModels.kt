package com.example.sih_2026.network

import kotlinx.serialization.Serializable

@Serializable
data class AudioChunk(
    val type: String = "audio_chunk",
    val call_id: String,
    val sequence: Int,
    val timestamp: Long,
    val sample_rate: Int = 16000,
    val encoding: String = "pcm16",
    val audio: String // Base64 encoded audio
)

@Serializable
data class RiskUpdate(
    val type: String = "risk_update",
    val call_id: String,
    val risk: RiskDetails,
    val voice: VoiceSignals,
    val context: ContextDetails,
    val decision: String // ALLOW, VERIFY, BLOCK
)

@Serializable
data class RiskDetails(
    val score: Int,
    val level: String // LOW, MEDIUM, HIGH, CRITICAL
)

@Serializable
data class VoiceSignals(
    val synthetic: Float,
    val speaker_similarity: Float,
    val replay: Float,
    val prosody: Float
)

@Serializable
data class ContextDetails(
    val financial_intent: Boolean,
    val urgency: Float,
    val reason_codes: List<String>
)

@Serializable
data class BaseEvent(
    val type: String,
    val call_id: String? = null
)
