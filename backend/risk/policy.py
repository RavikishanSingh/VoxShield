from enum import Enum

class Decision(str, Enum):
    ALLOW = "ALLOW"
    VERIFY = "VERIFY"
    MFA = "MFA"
    BLOCK = "BLOCK"

class PolicyEngine:
    def get_decision(self, risk_score: float) -> Decision:
        if risk_score < 30:
            return Decision.ALLOW
        elif risk_score < 60:
            return Decision.VERIFY
        elif risk_score < 85:
            return Decision.MFA
        else:
            return Decision.BLOCK

policy_engine = PolicyEngine()
