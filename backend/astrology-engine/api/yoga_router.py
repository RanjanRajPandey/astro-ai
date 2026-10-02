from fastapi import APIRouter, HTTPException
from models.yogas import YogaCalculationRequest, YogaCalculationResponse
from yogas.detector import detect_all_yogas

router = APIRouter(prefix="/engine/yogas", tags=["Classical Vedic Yogas & Doshas"])


@router.post("/calculate", response_model=YogaCalculationResponse)
def calculate_yogas_endpoint(request: YogaCalculationRequest) -> YogaCalculationResponse:
    try:
        return detect_all_yogas(request)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve)) from ve
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail=f"Yoga detection error: {str(exc)}"
        ) from exc
