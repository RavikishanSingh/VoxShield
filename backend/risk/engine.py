from typing import Dict

class RiskEngine:
    def __init__(self):
        self.weights = {
            "voice": 0.35,
            "speaker": 0.20,
            "replay": 0.10,
            "prosody": 0.10,
            "intent": 0.15,
            "context": 0.10
        }

    def calculate_score(self, evidence: Dict[str, float]) -> float:
        """
        Calculates a weighted risk score (0-100).
        evidence: dictionary of risk scores (0.0 to 1.0) for each factor.
        """
        score = 0.0
        for factor, weight in self.weights.items():
            factor_score = evidence.get(factor, 0.0)
            score += factor_score * weight

        return min(max(score * 100, 0.0), 100.0)

risk_engine = RiskEngine()
