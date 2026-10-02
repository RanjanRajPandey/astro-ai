from datetime import date, time
from typing import Dict, List, Optional
from pydantic import BaseModel, Field


class PlanetCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class PlanetPositionModel(BaseModel):
    planet: str
    longitude: float
    latitude: float
    speed_longitude: float
    sign: str
    sanskrit_sign: str
    sign_index: int
    sign_lord: str
    degree_in_sign: float
    degree_dms: str
    house: int
    nakshatra: str
    nakshatra_index: int
    nakshatra_lord: str
    pada: int
    is_retrograde: bool
    is_combust: bool
    angular_distance_from_sun: Optional[float] = None
    is_exalted: bool
    is_debilitated: bool
    is_moolatrikona: bool
    is_own_sign: bool
    dignity: str
    dispositor_relationship: str
    planetary_relationships: Dict[str, Dict[str, str]]


class PlanetCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    node_type: str
    ascendant_longitude: float
    ascendant_sign: str
    ascendant_sign_index: int
    ascendant_degree_in_sign: float
    ascendant_dms: str
    planets: List[PlanetPositionModel]
