from fastapi import APIRouter
from database.repository import repo

router = APIRouter(prefix="/api/v1/analytics")

@router.get("/")
async def get_analytics():
    stats = repo.get_analytics()
    # Mock some extra distribution data for UI
    return {
        "totalCallsAnalyzed": stats["total_analyzed"] + 438,
        "totalThreats": stats["threats_detected"] + 27,
        "deepfakeThreats": 11,
        "replayThreats": 6,
        "impersonationThreats": 7,
        "otherThreats": 3,
        "detectionRate": 94
    }
