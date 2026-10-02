from datetime import date, time
from typing import Optional
from pydantic import BaseModel, Field


class LocationResolveRequest(BaseModel):
    place_of_birth: str = Field(..., min_length=2, description="City/Place name")
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    timezone_id: Optional[str] = None


class LocationResolveResponse(BaseModel):
    resolved_place_name: str
    country_code: Optional[str] = None
    latitude: float
    longitude: float
    latitude_dms: str
    longitude_dms: str
    timezone_id: str
    utc_offset_hours: float
    is_dst: bool
    historical_note: Optional[str] = None
    local_datetime_iso: str
    utc_datetime_iso: str
    julian_day_ut: float
    delta_t_seconds: float
    julian_day_et: float
    birth_time_accurate: bool
