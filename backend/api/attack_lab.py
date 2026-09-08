from fastapi import APIRouter
from pydantic import BaseModel
from typing import Dict, Any
from risk.engine import risk_engine
from risk.policy import policy_engine

router = APIRouter(prefix="/api/v1/attack-lab", tags=["Attack Lab"])

class AttackRequest(BaseModel):
    attack_type: str

@router.post("/start")
async def start_attack(request: AttackRequest):
    attack_type = request.attack_type

    # Simulated evidence vectors
    simulated_evidence = {
        "voice_clone": {
            "voice": 0.94,
            "speaker": 0.68, # 0.32 similarity
            "replay": 0.08,
            "prosody": 0.81,
            "intent": 0.40,
            "context": 0.10
        },
        "replay": {
            "voice": 0.20,
            "speaker": 0.55,
            "replay": 0.94,
            "prosody": 0.10,
            "intent": 0.10,
            "context": 0.05
        },
        "ai_generated": {
            "voice": 0.91,
            "speaker": 0.62,
            "replay": 0.08,
            "prosody": 0.75,
            "intent": 0.30,
            "context": 0.10
        },
        "voice_conversion": {
            "voice": 0.72,
            "speaker": 0.45,
            "replay": 0.18,
            "prosody": 0.60,
            "intent": 0.20,
            "context": 0.10
        }
    }

    evidence = simulated_evidence.get(attack_type, {
        "voice": 0.05,
        "speaker": 0.02, # High similarity
        "replay": 0.02,
        "prosody": 0.05,
        "intent": 0.05,
        "context": 0.05
    })

    score = risk_engine.calculate_score(evidence)
    decision = policy_engine.get_decision(score)

    return {
        "status": "attack_simulated",
        "attack_type": attack_type,
        "voice": {
            "synthetic": evidence["voice"],
            "speaker_similarity": 1.0 - evidence["speaker"],
            "replay": evidence["replay"],
            "prosody": evidence["prosody"]
        },
        "risk": {
            "score": int(score),
            "level": "CRITICAL" if score > 85 else "HIGH" if score > 60 else "MEDIUM",
            "decision": decision
        }
    }
