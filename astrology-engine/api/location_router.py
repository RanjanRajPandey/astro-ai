from fastapi import APIRouter, HTTPException
from models.location import LocationResolveRequest, LocationResolveResponse
from coordinates.validator import (
    validate_coordinates,
    decimal_to_dms,
    lookup_place_in_gazetteer,
    CURATED_GAZETTEER,
)
from timezone.resolver import resolve_birth_timestamp

router = APIRouter(prefix="/api/v1/location", tags=["Location & Timezone"])


@router.post("/resolve", response_model=LocationResolveResponse)
def resolve_location_and_time(req: LocationResolveRequest) -> LocationResolveResponse:
    try:
        gazetteer_match = lookup_place_in_gazetteer(req.place_of_birth)
        if req.latitude is not None and req.longitude is not None:
            lat, lon = validate_coordinates(req.latitude, req.longitude)
            resolved_name = gazetteer_match.name if gazetteer_match else req.place_of_birth.strip()
            country_code = gazetteer_match.country_code if gazetteer_match else None
            tz_hint = req.timezone_id or (gazetteer_match.timezone_id if gazetteer_match else None)
        elif gazetteer_match is not None:
            lat, lon = gazetteer_match.latitude, gazetteer_match.longitude
            resolved_name = f"{gazetteer_match.name}, {gazetteer_match.state_or_region}"
            country_code = gazetteer_match.country_code
            tz_hint = req.timezone_id or gazetteer_match.timezone_id
        else:
            raise ValueError(
                f"Could not resolve coordinates for place '{req.place_of_birth}'. "
                "Please provide valid latitude/longitude or select a recognized city."
            )

        resolved_time = resolve_birth_timestamp(
            birth_date=req.date_of_birth,
            birth_time=req.time_of_birth,
            latitude=lat,
            longitude=lon,
            explicit_timezone_id=tz_hint,
        )

        return LocationResolveResponse(
            resolved_place_name=resolved_name,
            country_code=country_code,
            latitude=lat,
            longitude=lon,
            latitude_dms=decimal_to_dms(lat, is_latitude=True),
            longitude_dms=decimal_to_dms(lon, is_latitude=False),
            timezone_id=resolved_time.timezone_id,
            utc_offset_hours=resolved_time.utc_offset_hours,
            is_dst=resolved_time.is_dst,
            historical_note=resolved_time.historical_note,
            local_datetime_iso=resolved_time.local_datetime_iso,
            utc_datetime_iso=resolved_time.utc_datetime_iso,
            julian_day_ut=resolved_time.julian_day_ut,
            delta_t_seconds=resolved_time.delta_t_seconds,
            julian_day_et=resolved_time.julian_day_et,
            birth_time_accurate=resolved_time.birth_time_accurate,
        )
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.get("/search")
def search_cities(q: str = ""):
    query = q.strip().lower()
    results = []
    seen = set()
    for entry in CURATED_GAZETTEER.values():
        key = (entry.name, entry.country_code)
        if key in seen:
            continue
        if not query or query in entry.name.lower() or query in entry.state_or_region.lower():
            seen.add(key)
            results.append(
                {
                    "name": entry.name,
                    "stateOrRegion": entry.state_or_region,
                    "countryCode": entry.country_code,
                    "latitude": entry.latitude,
                    "longitude": entry.longitude,
                    "timezoneId": entry.timezone_id,
                }
            )
    return {"results": results[:15]}
