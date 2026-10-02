from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class NakshatraCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class NakshatraPlacementModel(BaseModel):
    body_name: str
    longitude: float
    rashi_sign: str
    nakshatra_name: str
    nakshatra_index: int
    pada: int
    pada_navamsha_sign: str
    ruler_planet: str
    degree_in_nakshatra: float
    degree_in_nakshatra_dms: str
    elapsed_fraction: float
    remaining_fraction: float
    deity: str
    gana: str
    nadi: str
    yoni: str
    symbol: str
    tara_number_from_moon: int
    tara_name_from_moon: str
    tara_quality: str
    relationship_to_nakshatra_lord: str


class NakshatraCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    janma_nakshatra: str
    janma_nakshatra_index: int
    janma_pada: int
    janma_nakshatra_lord: str
    janma_rashi: str
    moon_elapsed_fraction: float
    moon_remaining_fraction: float
    placements: List[NakshatraPlacementModel]
