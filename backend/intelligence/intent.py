from typing import List, Dict

class IntentEngine:
    def __init__(self):
        # Multilingual Taxonomy: English + Hindi (Hinglish context)
        self.taxonomy = {
            "FINANCIAL_FRAUD": [
                "otp", "pin", "password", "upi", "bank", "transfer", "account", "money", "rupees", "cash",
                "bhej de", "khata", "paise", "nikal", "transaction", "payment", "fund"
            ],
            "AUTHORITY_IMPERSONATION": [
                "police", "cbi", "inspector", "manager", "official", "government", "court",
                "income tax", "rbi", "bank manager", "customer care", "officer"
            ],
            "URGENCY_COERCION": [
                "immediately", "urgently", "fast", "now", "hurry", "arrest", "block", "penalty",
                "abhi ke abhi", "jaldi", "turant", "jail", "complaint", "last chance"
            ]
        }

    def analyze(self, transcript: str) -> Dict[str, any]:
        lower = transcript.lower()
        detected_intents = []
        reason_codes = []
        risk_boost = 0.0

        for category, keywords in self.taxonomy.items():
            matches = [k for k in keywords if k in lower]
            if matches:
                detected_intents.append(category)
                reason_codes.append(f"Detected {category} triggers: {', '.join(matches[:3])}")

                if category == "FINANCIAL_FRAUD":
                    risk_boost += 0.4
                elif category == "AUTHORITY_IMPERSONATION":
                    risk_boost += 0.3
                elif category == "URGENCY_COERCION":
                    risk_boost += 0.2

        return {
            "intents": detected_intents,
            "reason_codes": reason_codes,
            "risk_boost": min(risk_boost, 0.9)
        }
