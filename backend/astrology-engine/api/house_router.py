from dataclasses import asdict
from fastapi import APIRouter, HTTPException
from houses.calculator import calculate_houses_and_ascendant
from models.houses import (
    AscendantModel,
    HouseCalculationRequest,
    HouseCalculationResponse,
    HouseModel,
)
from timezone.resolver import resolve_birth_timestamp

router = APIRouter(prefix="/api/v1/houses", tags=["Ascendant & House Engine"])


@router.post("/calculate", response_model=HouseCalculationResponse)
def calculate_houses_endpoint(req: HouseCalculationRequest) -> HouseCalculationResponse:
    try:
        resolved_time = resolve_birth_timestamp(
            birth_date=req.date_of_birth,
            birth_time=req.time_of_birth,
            latitude=req.latitude,
            longitude=req.longitude,
            explicit_timezone_id=req.timezone_id,
        )

        res = calculate_houses_and_ascendant(
            jd_ut=resolved_time.julian_day_ut,
            latitude=req.latitude,
            longitude=req.longitude,
            ayanamsha_type=req.ayanamsha_type,
            node_type=req.node_type,
            rahu_ketu_trinal_aspects=req.rahu_ketu_trinal_aspects,
        )

        return HouseCalculationResponse(
            utc_datetime_iso=resolved_time.utc_datetime_iso,
            julian_day_ut=res.julian_day_ut,
            ayanamsha_type=res.ayanamsha_type,
            ayanamsha_value=res.ayanamsha_value,
            house_system=res.house_system,
            ascendant=AscendantModel(**asdict(res.ascendant)),
            houses=[HouseModel(**asdict(h)) for h in res.houses],
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
