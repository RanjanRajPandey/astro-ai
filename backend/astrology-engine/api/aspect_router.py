from fastapi import APIRouter, HTTPException
from aspects.drishti import calculate_all_aspects
from models.aspects import AspectCalculationRequest, AspectCalculationResponse

router = APIRouter(prefix="/engine/aspects", tags=["Planetary Aspects (Drishti)"])


@router.post("/calculate", response_model=AspectCalculationResponse)
def calculate_aspects_endpoint(request: AspectCalculationRequest) -> AspectCalculationResponse:
    try:
        return calculate_all_aspects(request)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve)) from ve
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail=f"Drishti calculation error: {str(exc)}"
        ) from exc
