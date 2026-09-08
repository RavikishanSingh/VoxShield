from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict

router = APIRouter(prefix="/api/v1/profiles", tags=["Voice Profiles"])

# Simulated in-memory "Trusted Voice Print" database
trusted_vault: Dict[str, Dict] = {}

class EnrollmentRequest(BaseModel):
    id: str
    name: str
    embedding_stub: str # In production, this would be a real 512-d vector

@router.post("/enroll")
async def enroll_voice(request: EnrollmentRequest):
    trusted_vault[request.id] = {
        "name": request.name,
        "embedding": request.embedding_stub,
        "enrolled_at": "2026-09-08T12:00:00Z"
    }
    return {"status": "success", "message": f"Voice profile for {request.name} enrolled."}

@router.get("/")
async def get_profiles():
    return list(trusted_vault.values())

def get_trusted_voice(profile_id: str):
    return trusted_vault.get(profile_id)
