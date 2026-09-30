from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class YogaEvaluationEntry(BaseModel):
    yoga_code: str
    name: str
    sanskrit_name: str
    category: str = Field(
        ...,
        description="PANCHA_MAHAPURUSHA, LUNAR_YOGA, SOLAR_YOGA, RAJA_YOGA, DHANA_YOGA, VIPARITA_RAJA_YOGA, SPECIAL_YOGA, or DOSHA",
    )
    definition: str
    classical_Effect: str
    required_conditions: List[str]
    detected_conditions: List[str]
    planets_involved: List[str]
    houses_involved: List[int]
    status: str = Field(..., description="ACTIVE, CANCELLED_OR_MITIGATED, or NOT_FORMED")
    strength: str = Field(..., description="VERY_STRONG, STRONG, MODERATE, MITIGATED, or INACTIVE")
    is_benefic: bool = Field(..., description="True for auspicious Yogas, False for challenging Doshas/Arishtas")


class YogaCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class YogaCalculationResponse(BaseModel):
    utc_datetime_iso: str
    julian_day_ut: float
    ascendant_sign: str
    moon_sign: str
    ayanamsha_type: str
    active_yoga_count: int
    active_dosha_count: int
    mitigated_count: int
    total_evaluated_count: int
    active_yogas: List[YogaEvaluationEntry]
    all_evaluated_yogas: List[YogaEvaluationEntry]
