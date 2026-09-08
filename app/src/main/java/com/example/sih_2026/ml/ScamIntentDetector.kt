package com.example.sih_2026.ml

import android.util.Log
import com.example.sih_2026.domain.model.ScamAnalysisResult

class ScamIntentDetector {

    private val TAG = "ScamIntentDetector (Taxonomy Engine)"

    // Intent Taxonomy Weights
    private val weightFinancial = 35f
    private val weightImpersonation = 25f
    private val weightUrgency = 20f
    private val weightThreat = 20f

    private val taxonomy = mapOf(
        "Financial Fraud" to listOf("otp", "pin", "password", "upi", "bank", "transfer", "account", "card", "cvv", "money", "rupees", "₹"),
        "Authority Impersonation" to listOf("police", "cbi", "manager", "official", "government", "bank manager", "customer care", "inspector"),
        "Social Engineering (Urgency)" to listOf("immediately", "urgently", "fast", "now", "hurry", "abhi", "deadline", "last chance"),
        "Social Engineering (Fear/Threat)" to listOf("arrest", "case", "penalty", "block", "suspend", "court", "complaint")
    )

    fun analyzeTranscript(transcript: String): ScamAnalysisResult {
        val lower = transcript.lowercase()
        val detectedCategories = mutableSetOf<String>()
        val triggers = mutableListOf<String>()
        
        var financialScore = 0f
        var impersonationScore = 0f
        var urgencyScore = 0f
        var threatScore = 0f

        // Taxonomical Scan
        taxonomy.forEach { (category, keywords) ->
            val matches = keywords.filter { lower.contains(it) }
            if (matches.isNotEmpty()) {
                detectedCategories.add(category)
                triggers.addAll(matches.map { it.uppercase() })
                
                when (category) {
                    "Financial Fraud" -> financialScore = weightFinancial
                    "Authority Impersonation" -> impersonationScore = weightImpersonation
                    "Social Engineering (Urgency)" -> urgencyScore = weightUrgency
                    "Social Engineering (Fear/Threat)" -> threatScore = weightThreat
                }
            }
        }

        val totalRisk = (financialScore + impersonationScore + urgencyScore + threatScore).coerceIn(0f, 100f)
        
        // Explainability Reasons
        val reasons = mutableListOf<String>()
        if (financialScore > 0) reasons.add("Requested sensitive financial credentials or transfer.")
        if (impersonationScore > 0) reasons.add("Attempted to establish authority via impersonation.")
        if (urgencyScore > 0) reasons.add("Used high-pressure urgency tactics.")
        if (threatScore > 0) reasons.add("Used fear-based threats or legal coercion.")

        Log.d(TAG, "Taxonomy Analysis: Risk=$totalRisk, Categories=$detectedCategories")

        return ScamAnalysisResult(
            transcript = transcript,
            conversationRisk = totalRisk,
            detectedIntents = detectedCategories.toList(),
            reasons = reasons,
            semanticConfidence = if (totalRisk > 0) 85f else 10f
        )
    }
}
