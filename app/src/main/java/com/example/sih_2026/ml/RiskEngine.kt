package com.example.sih_2026.ml

import android.util.Log

import com.example.sih_2026.domain.model.RiskAssessment
import com.example.sih_2026.domain.model.RiskLevel
import java.util.ArrayDeque

/**
 * Handles Temporal Aggregation of risk scores over a sliding window.
 * This prevents sudden spikes (false positives) from triggering CRITICAL alerts immediately.
 */
class TemporalRiskAggregator(private val windowSize: Int = 5) {
    private val window = ArrayDeque<Float>()

    fun aggregate(currentScore: Float): Float {
        if (window.size >= windowSize) window.removeFirst()
        window.addLast(currentScore)
        return window.average().toFloat()
    }
    
    fun isTrendRising(): Boolean {
        if (window.size < 3) return false
        val list = window.toList()
        return list[list.size - 1] > list[list.size - 2] && list[list.size - 2] > list[list.size - 3]
    }
}

class RiskEngine {

    private val TAG = "RiskEngine (XAI)"
    private val aggregator = TemporalRiskAggregator(windowSize = 5)

    /**
     * Combines multi-signal risks with XAI (Explainable AI) output:
     * Final Risk = (0.45 * Voice) + (0.35 * Intent) + (0.20 * Metadata)
     */
    fun evaluateRisk(
        voiceSpoofProbability: Float, // 0.0 to 1.0
        conversationRiskScore: Float, // 0 to 100
        callerRiskScore: Float,       // 0 to 100
        detectedReasons: List<String>
    ): RiskAssessment {
        val voiceRisk = voiceSpoofProbability * 100f

        val instantaneousScore = (0.45f * voiceRisk) +
                (0.35f * conversationRiskScore) +
                (0.20f * callerRiskScore)

        // Apply Temporal Aggregation
        val aggregatedScore = aggregator.aggregate(instantaneousScore)
        val scoreInt = aggregatedScore.toInt().coerceIn(0, 100)

        val level = when {
            scoreInt <= 25 -> RiskLevel.LOW
            scoreInt <= 55 -> RiskLevel.SUSPICIOUS
            scoreInt <= 75 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        // Explainability Logic
        val xaiReasons = mutableListOf<String>()
        xaiReasons.addAll(detectedReasons)
        
        if (voiceRisk > 70) xaiReasons.add("Acoustic Signature: High probability of synthetic/deepfake origin.")
        if (aggregator.isTrendRising()) xaiReasons.add("Risk Trend: Sustained increase in suspicious activity detected.")
        if (scoreInt > 75 && voiceRisk > 60) xaiReasons.add("Critical Trigger: AI Voice + Fraudulent Intent combination.")

        val (title, description) = when (level) {
            RiskLevel.LOW -> Pair("🟢 Secure Session", "No threats detected.")
            RiskLevel.SUSPICIOUS -> Pair("🟡 Caution Advised", "Unusual speech patterns or intent triggers detected.")
            RiskLevel.HIGH -> Pair("🟠 High Threat Alert", "Possible AI-generated voice or targeted scam detected.")
            RiskLevel.CRITICAL -> Pair("🔴 Critical Risk - AI SCAM", "Extremely high probability of AI Voice Fraud. Protect your data.")
        }

        Log.d(TAG, "Aggregated Risk: $scoreInt ($level). Trend Rising: ${aggregator.isTrendRising()}")

        return RiskAssessment(
            finalScore = scoreInt,
            riskLevel = level,
            title = title,
            description = description,
            reasons = xaiReasons.distinct(),
            isTemporalAlert = aggregator.isTrendRising()
        )
    }
}
