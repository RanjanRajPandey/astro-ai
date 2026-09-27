from fastapi import APIRouter, HTTPException

from divisional.shodashavarga import calculate_shodashavarga
from models.divisional import DivisionalCalculationRequest, DivisionalCalculationResponse

router = APIRouter(prefix="/api/v1/divisional", tags=["Divisional Charts (Shodashavarga)"])


@router.post("/calculate", response_model=DivisionalCalculationResponse)
def post_calculate_divisional(
    request: DivisionalCalculationRequest,
) -> DivisionalCalculationResponse:
    try:
        return calculate_shodashavarga(request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
