from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class HouseCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"
    rahu_ketu_trinal_aspects: bool = False


class AscendantModel(BaseModel):
    longitude: float
    sign: str
    sanskrit_sign: str
    sign_index: int
    degree_in_sign: float
    degree_dms: str
    nakshatra: str
    nakshatra_index: int
    nakshatra_lord: str
    pada: int
    lagna_lord: str
    lagna_lord_sign: str
    lagna_lord_house: int
    lagna_lord_dignity: str
    chandra_lagna_sign: str
    surya_lagna_sign: str
    arudha_lagna_sign: str
    arudha_lagna_house: int
    upapada_lagna_sign: str
    upapada_lagna_house: int


class HouseAspectModel(BaseModel):
    source_planet: str
    source_house: int
    source_sign: str
    aspect_house_distance: int
    aspect_type: str
    rule: str


class HouseModel(BaseModel):
    house_number: int
    sign: str
    sanskrit_sign: str
    sign_index: int
    lord_planet: str
    lord_placed_in_house: int
    lord_placed_in_sign: str
    lord_dignity: str
    degree_cusp: float
    sripati_cusp_longitude: float
    sripati_start_longitude: float
    sripati_end_longitude: float
    occupants: List[str]
    chalit_occupants: List[str]
    aspects_received: List[HouseAspectModel]
    purushartha: str
    classifications: List[str]
    significations: List[str]
    baseline_strength_score: float
    strength_grade: str


class HouseCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    house_system: str
    ascendant: AscendantModel
    houses: List[HouseModel]
