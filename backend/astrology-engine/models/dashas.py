from datetime import date, datetime, time
from typing import List, Optional
from pydantic import BaseModel, Field


class DashaCalculationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"
    target_datetime_iso: Optional[str] = None


class DashaPeriodNode(BaseModel):
    planet: str
    level: int
    level_name: str
    start_date_time: str
    end_date_time: str
    unclamped_start_date_time: str
    duration_days: float
    duration_years: float
    is_currently_active: bool = False
    is_birth_balance_period: bool = False
    sub_periods: List["DashaPeriodNode"] = Field(default_factory=list)


DashaPeriodNode.model_rebuild()


class ActiveDashaStackItem(BaseModel):
    level: int
    level_name: str
    planet: str
    start_date_time: str
    end_date_time: str
    unclamped_start_date_time: str
    duration_days: float
    elapsed_percentage: float


class DashaCalculationResponse(BaseModel):
    birth_utc_datetime_iso: str
    target_utc_datetime_iso: str
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    year_length_days: float
    moon_longitude: float
    janma_nakshatra: str
    janma_pada: int
    birth_dasha_lord: str
    moon_elapsed_fraction: float
    moon_remaining_fraction: float
    birth_balance_years: float
    birth_balance_days: float
    birth_balance_formatted: str
    active_stack: List[ActiveDashaStackItem]
    active_sookshma_periods: List[DashaPeriodNode]
    active_prana_periods: List[DashaPeriodNode]
    mahadashas: List[DashaPeriodNode]
