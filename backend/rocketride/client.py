from typing import Dict, List, Any
import random

class RocketRideClient:
    """
    Simulation of the RocketRide Intelligence Pipeline.
    Synthesizes multi-signal evidence into an explainable decision.
    """
    def __init__(self, api_key: str = "DEMO"):
        self.api_key = api_key

    def synthesize_evidence(self, voice_evidence: Dict[str, float], semantic_intent: Dict[str, Any]) -> Dict[str, Any]:
        """
        Processes voice scores and semantic intents to produce a reasoned output.
        """
        voice_prob = voice_evidence.get("voice", 0.0)
        speaker_risk = voice_evidence.get("speaker", 0.0)
        replay_prob = voice_evidence.get("replay", 0.0)

        intents = semantic_intent.get("intents", [])
        intent_boost = semantic_intent.get("risk_boost", 0.0)

        # 1. Base Synthesis Logic
        # Highly weighted towards Voice Prob and Speaker mismatch
        raw_score = (voice_prob * 0.4) + (speaker_risk * 0.3) + (replay_prob * 0.1) + (intent_boost * 0.2)

        # 2. Heuristic Correlations (The "Brain" part)
        reasons = []
        if voice_prob > 0.8:
            reasons.append("HIGH_CONFIDENCE_DEEPFAKE")
        if speaker_risk > 0.7:
            reasons.append("SPEAKER_IDENTITY_MISMATCH")
        if "FINANCIAL_FRAUD" in intents:
            reasons.append("SENSITIVE_FINANCIAL_INTENT")
            # Cross-correlation boost: AI Voice + Money Request = Extreme Danger
            if voice_prob > 0.6:
                raw_score = min(1.0, raw_score + 0.3)
                reasons.append("CRITICAL_CROSS_MODAL_THREAT")

        if "URGENCY_COERCION" in intents:
            reasons.append("HIGH_URGENCY_LANGUAGE")

        # 3. Confidence Calculation
        # Confidence is higher if multiple signals agree
        agreement_factor = 1.0 if (voice_prob > 0.5 and intent_boost > 0.3) else 0.8
        confidence = min(0.98, agreement_factor + random.uniform(0.01, 0.05))

        return {
            "total_risk": float(raw_score),
            "confidence": float(confidence),
            "reason_codes": list(set(reasons)),
            "pipeline_version": "RocketRide-v2-Mobile"
        }

rocket_ride = RocketRideClient()
