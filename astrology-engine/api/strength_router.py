from fastapi import APIRouter, HTTPException
from models.strength import (
    BhavaBalaCalculationRequest,
    BhavaBalaCalculationResponse,
    ShadbalaCalculationRequest,
    ShadbalaCalculationResponse,
)
from strength.bhavabala import calculate_bhava_bala
from strength.shadbala import calculate_shadbala_and_vimshopaka

router = APIRouter(
    prefix="/engine/strength",
    tags=["Planetary & House Strength (Shadbala, Vimshopaka, Bhava Bala)"],
)


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


@router.post("/bhavabala", response_model=BhavaBalaCalculationResponse)
def calculate_bhavabala_endpoint(
    request: BhavaBalaCalculationRequest,
) -> BhavaBalaCalculationResponse:
    try:
        return calculate_bhava_bala(request)
    except ValueError as ve:
        raise HTTPException(status_code=400, detail=str(ve)) from ve
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail=f"Bhava Bala calculation error: {str(exc)}"
        ) from exc
