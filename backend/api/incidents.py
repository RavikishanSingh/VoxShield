from fastapi import APIRouter
from database.repository import repo

router = APIRouter(prefix="/api/v1/incidents")

@router.get("/")
async def get_incidents():
    return repo.get_incidents()
