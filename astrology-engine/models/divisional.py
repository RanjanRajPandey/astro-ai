from datetime import date, time
from typing import Dict, List, Optional
from pydantic import BaseModel, Field


class DivisionalCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"
    varga_code: Optional[str] = None


class DivisionalPlanetPlacementModel(BaseModel):
    planet: str
    d1_longitude: float
    d1_sign: str
    d1_house: int
    varga_sign: str
    varga_sanskrit_sign: str
    varga_sign_index: int
    varga_sign_lord: str
    varga_house: int
    part_number: int
    is_vargottama: bool
    dignity_in_varga: str
    retrograde: bool
    combust: bool
    shashtiamsha_name: Optional[str] = None
    shashtiamsha_quality: Optional[str] = None


class DivisionalHouseSummaryModel(BaseModel):
    house_number: int
    sign: str
    sanskrit_sign: str
    sign_index: int
    lord_planet: str
    occupants: List[str]


class DivisionalChartModel(BaseModel):
    varga_code: str
    division_number: int
    sanskrit_name: str
    title: str
    domain_signification: str
    ascendant_sign: str
    ascendant_sanskrit_sign: str
    ascendant_sign_index: int
    ascendant_lord: str
    is_ascendant_vargottama: bool
    vargottama_planets: List[str]
    planets: List[DivisionalPlanetPlacementModel]
    houses: List[DivisionalHouseSummaryModel]


class DivisionalCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    d1_ascendant_sign: str
    vargottama_Swapna_summary: List[str] = Field(default_factory=list)
    charts: List[DivisionalChartModel]
