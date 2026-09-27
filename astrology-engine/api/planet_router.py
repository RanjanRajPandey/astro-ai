from dataclasses import asdict
from fastapi import APIRouter, HTTPException
from models.planets import PlanetCalculationRequest, PlanetCalculationResponse, PlanetPositionModel
from planets.calculator import calculate_planetary_positions
from timezone.resolver import resolve_birth_timestamp

router = APIRouter(prefix="/api/v1/planets", tags=["Planetary Calculation Engine"])


@router.post("/calculate", response_model=PlanetCalculationResponse)
def calculate_planets_endpoint(req: PlanetCalculationRequest) -> PlanetCalculationResponse:
    try:
        resolved_time = resolve_birth_timestamp(
            birth_date=req.date_of_birth,
            birth_time=req.time_of_birth,
            latitude=req.latitude,
            longitude=req.longitude,
            explicit_timezone_id=req.timezone_id,
        )

        calc_result = calculate_planetary_positions(
            jd_ut=resolved_time.julian_day_ut,
            latitude=req.latitude,
            longitude=req.longitude,
            ayanamsha_type=req.ayanamsha_type,
            node_type=req.node_type,
        )

        return PlanetCalculationResponse(
            utc_datetime_iso=resolved_time.utc_datetime_iso,
            julian_day_ut=calc_result.julian_day_ut,
            ayanamsha_type=calc_result.ayanamsha_type,
            ayanamsha_value=calc_result.ayanamsha_value,
            node_type=calc_result.node_type,
            ascendant_longitude=calc_result.ascendant_longitude,
            ascendant_sign=calc_result.ascendant_sign,
            ascendant_sign_index=calc_result.ascendant_sign_index,
            ascendant_degree_in_sign=calc_result.ascendant_degree_in_sign,
            ascendant_dms=calc_result.ascendant_dms,
            planets=[PlanetPositionModel(**asdict(p)) for p in calc_result.planets],
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
