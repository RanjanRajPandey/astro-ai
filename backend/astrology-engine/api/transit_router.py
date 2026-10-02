from fastapi import APIRouter, HTTPException
from models.transits import TransitCalculationRequest, TransitCalculationResponse
from transits.gochar import calculate_gochar_transits

router = APIRouter(prefix="/engine/transits", tags=["Planetary Transits (Gochar)"])


@router.post("/calculate", response_model=TransitCalculationResponse)
def post_calculate_transits(request: TransitCalculationRequest) -> TransitCalculationResponse:
    try:
        return calculate_gochar_transits(request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
