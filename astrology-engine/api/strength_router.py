from fastapi import APIRouter, HTTPException
from models.strength import ShadbalaCalculationRequest, ShadbalaCalculationResponse
from strength.shadbala import calculate_shadbala_and_vimshopaka

router = APIRouter(prefix="/engine/strength", tags=["Planetary Strength (Shadbala & Vimshopaka)"])


@router.post("/shadbala", response_model=ShadbalaCalculationResponse)
def calculate_shadbala_endpoint(
    request: ShadbalaCalculationRequest,
) -> ShadbalaCalculationResponse:
    try:
        return calculate_shadbala_and_vimshopaka(request)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve)) from ve
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail=f"Shadbala calculation error: {str(exc)}"
        ) from exc
