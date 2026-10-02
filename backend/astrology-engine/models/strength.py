from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class SthanaBalaBreakdown(BaseModel):
    uchcha_bala: float
    saptavargaja_bala: float
    ojhayugmarasyamsa_bala: float
    kendradi_bala: float
    drekkana_bala: float
    total: float


class KalaBalaBreakdown(BaseModel):
    nathonnatha_bala: float
    paksha_bala: float
    tribhaga_bala: float
    vara_bala: float
    ayana_bala: float
    total: float


class PlanetStrengthEntry(BaseModel):
    planet: str
    sign: str
    house: int
    d1_dignity: str
    is_retrograde: bool
    sthana_bala: float
    sthana_breakdown: SthanaBalaBreakdown
    dig_bala: float
    kala_bala: float
    kala_breakdown: KalaBalaBreakdown
    chesta_bala: float
    naisargika_bala: float
    drik_bala: float
    total_shadbala_virupas: float
    total_shadbala_rupas: float
    required_minimum_rupas: float
    shadbala_ratio: float
    vimshopaka_bala: float = Field(..., ge=0.0, le=20.0, description="16-Varga Shodashavarga Vimshopaka score out of 20.0")
    vimshopaka_percentage: float
    strength_grade: str = Field(..., description="VERY_STRONG, ADEQUATE, MODERATE, or WEAK")
    rank: int = Field(..., ge=1, le=9)


class ShadbalaCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class ShadbalaCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ascendant_sign: str
    ayanamsha_type: str
    strongest_planet: str
    weakest_planet: str
    planets: List[PlanetStrengthEntry]


class HouseStrengthEntry(BaseModel):
    house_number: int = Field(..., ge=1, le=12)
    sign: str
    sanskrit_sign: str
    lord_planet: str
    sign_nature: str = Field(..., description="NARA_BIPED, JALACHARA_WATER, KEETA_INSECT, or CHATUSHPADA_QUADRUPED")
    purushartha: str = Field(..., description="DHARMA, ARTHA, KAMA, or MOKSHA")
    domain_title: str
    occupants: List[str]
    bhavadhipati_bala: float
    bhava_dig_bala: float
    bhava_drishti_bala: float
    occupant_factor: float
    total_bhava_bala_virupas: float
    total_bhava_bala_rupas: float
    strength_grade: str = Field(..., description="VERY_STRONG, STRONG, MODERATE, or WEAK")
    rank: int = Field(..., ge=1, le=12)


class PurusharthaSummary(BaseModel):
    purushartha: str
    houses: List[int]
    average_rupas: float
    dominant_house: int


class BhavaBalaCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class BhavaBalaCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ascendant_sign: str
    ayanamsha_type: str
    strongest_house: int
    weakest_house: int
    average_rupas: float
    purushartha_summaries: List[PurusharthaSummary]
    houses: List[HouseStrengthEntry]
