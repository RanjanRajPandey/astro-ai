from dataclasses import asdict
from fastapi import APIRouter, HTTPException
from models.nakshatra import (
    NakshatraCalculationRequest,
    NakshatraCalculationResponse,
    NakshatraPlacementModel,
)
from nakshatra.calculator import calculate_nakshatras
from timezone.resolver import resolve_birth_timestamp

router = APIRouter(prefix="/api/v1/nakshatra", tags=["Nakshatra Engine"])


@router.post("/calculate", response_model=NakshatraCalculationResponse)
def calculate_nakshatra_endpoint(
    req: NakshatraCalculationRequest,
) -> NakshatraCalculationResponse:
    try:
        resolved_time = resolve_birth_timestamp(
            birth_date=req.date_of_birth,
            birth_time=req.time_of_birth,
            latitude=req.latitude,
            longitude=req.longitude,
            explicit_timezone_id=req.timezone_id,
        )

        res = calculate_nakshatras(
            jd_ut=resolved_time.julian_day_ut,
            latitude=req.latitude,
            longitude=req.longitude,
            ayanamsha_type=req.ayanamsha_type,
            node_type=req.node_type,
        )

        return NakshatraCalculationResponse(
            utc_datetime_iso=resolved_time.utc_datetime_iso,
            julian_day_ut=res.julian_day_ut,
            ayanamsha_type=res.ayanamsha_type,
            ayanamsha_value=res.ayanamsha_value,
            janma_nakshatra=res.janma_nakshatra,
            janma_nakshatra_index=res.janma_nakshatra_index,
            janma_pada=res.janma_pada,
            janma_nakshatra_lord=res.janma_nakshatra_lord,
            janma_rashi=res.janma_rashi,
            moon_elapsed_fraction=res.moon_elapsed_fraction,
            moon_remaining_fraction=res.moon_remaining_fraction,
            placements=[NakshatraPlacementModel(**asdict(pl)) for pl in res.placements],
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
