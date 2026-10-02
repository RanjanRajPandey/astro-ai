from fastapi import APIRouter, HTTPException

from dashas.vimshottari import calculate_vimshottari_dasha
from models.dashas import DashaCalculationRequest, DashaCalculationResponse

router = APIRouter(prefix="/api/v1/dashas", tags=["Dashas"])


@router.post("/calculate", response_model=DashaCalculationResponse)
def post_calculate_dashas(request: DashaCalculationRequest) -> DashaCalculationResponse:
    try:
        return calculate_vimshottari_dasha(request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
