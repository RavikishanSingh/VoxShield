from typing import Dict, Any
from rocketride.client import rocket_ride

class EvidenceSynthesizer:
    def synthesize(self, ml_results: Dict[str, float], intent_results: Dict[str, Any]) -> Dict[str, Any]:
        """
        Orchestrates the final synthesis using RocketRide logic.
        """
        # Convert raw ML results to a voice evidence vector
        voice_evidence = {
            "voice": ml_results.get("deepfake", 0.0),
            "speaker": 1.0 - ml_results.get("speaker", 1.0), # similarity -> risk
            "replay": ml_results.get("replay", 0.0),
            "prosody": ml_results.get("prosody", 0.0)
        }

        # Invoke RocketRide for "Brain" level synthesis
        synthesis = rocket_ride.synthesize_evidence(voice_evidence, intent_results)

        # Merge with raw signals for full visibility
        return {
            "risk_score": synthesis["total_risk"],
            "confidence": synthesis["confidence"],
            "reason_codes": synthesis["reason_codes"],
            "raw_voice": voice_evidence,
            "raw_intent": intent_results
        }

evidence_synthesizer = EvidenceSynthesizer()
