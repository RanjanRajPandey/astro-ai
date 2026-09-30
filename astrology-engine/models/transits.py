from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class TransitPlanetEntry(BaseModel):
    planet: str
    natal_sign: str
    natal_house_from_lagna: int
    transit_longitude: float
    transit_sign: str
    transit_sanskrit_sign: str
    transit_degree_dms: str
    transit_nakshatra: str
    transit_pada: int
    is_retrograde: bool
    house_from_moon: int = Field(..., ge=1, le=12)
    house_from_lagna: int = Field(..., ge=1, le=12)
    is_benefic_from_moon: bool
    vedha_obstructed: bool
    vedha_obstructor: Optional[str] = None
    gochar_status: str = Field(
        ...,
        description="FAVORABLE, VEDHA_OBSTRUCTED, or NEUTRAL_OR_CHALLENGING",
    )
    tara_bala_category: str
    is_tara_favorable: bool
    classical_summary: str


class SadeSatiStatus(BaseModel):
    sade_sati_active: bool
    dhaiya_active: bool
    phase: str = Field(
        ...,
        description="RISING_12TH, PEAK_JANMA_1ST, SETTING_2ND, DHAIYA_KANTAKA_4TH, DHAIYA_ASHTAMA_8TH, or NONE",
    )
    saturn_transit_sign: str
    saturn_house_from_moon: int
    saturn_house_from_lagna: int
    description: str


class DoubleTransitHouseEntry(BaseModel):
    house_number: int = Field(..., ge=1, le=12)
    sign: str
    sanskrit_sign: str
    domain_title: str
    jupiter_influence: Optional[str] = None
    saturn_influence: Optional[str] = None
    is_activated: bool


class TransitCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    transit_datetime_utc: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class TransitCalculationResponse(BaseModel):
    natal_utc_datetime_iso: str
    transit_utc_datetime_iso: str
    transit_julian_day_ut: float
    natal_ascendant_sign: str
    natal_moon_sign: str
    natal_moon_nakshatra: str
    ayanamsha_type: str
    favorable_transit_count: int
    vedha_obstructed_count: int
    sade_sati: SadeSatiStatus
    double_transit_houses: List[DoubleTransitHouseEntry]
    planets: List[TransitPlanetEntry]
