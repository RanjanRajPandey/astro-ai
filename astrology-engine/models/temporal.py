from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class DomainWindowEvaluation(BaseModel):
    domain_code: str
    domain_title: str
    primary_houses: List[int]
    natal_promise_score: float
    dasha_activation_score: float
    transit_confluence_score: float
    overall_confluence_score: float
    window_classification: str = Field(
        ...,
        description="HIGH_OPPORTUNITY, FAVORABLE_GROWTH, STEADY_CONSOLIDATION, or CAUTION_AND_REMEDY",
    )
    double_transit_triggered: bool
    supporting_factors: List[str]
    challenging_factors: List[str]


class TemporalForecastWindow(BaseModel):
    window_index: int
    window_label: str
    start_utc: str
    end_utc: str
    midpoint_utc: str
    mahadasha_lord: str
    antardasha_lord: str
    pratyantardasha_lord: str
    jupiter_transit_sign: str
    saturn_transit_sign: str
    sade_sati_phase: str
    double_transit_houses: List[int]
    overall_window_score: float
    dominant_domain: str
    domain_evaluations: List[DomainWindowEvaluation]


class DomainTimelineSummary(BaseModel):
    domain_code: str
    domain_title: str
    primary_houses: List[int]
    karaka_planets: List[str]
    natal_promise_score: float
    average_confluence_score: float
    peak_score: float
    peak_window_label: str
    peak_window_start_utc: str
    peak_window_end_utc: str
    current_classification: str
    executive_summary: str


class TemporalAnalysisRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    anchor_datetime_utc: Optional[str] = None
    window_count: int = Field(default=6, ge=1, le=12)
    window_Step_days: int = Field(default=60, ge=15, le=180)
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class TemporalAnalysisResponse(BaseModel):
    natal_utc_datetime_iso: str
    anchor_utc_datetime_iso: str
    natal_ascendant_sign: str
    natal_moon_sign: str
    ayanamsha_type: str
    window_count: int
    best_overall_window_label: str
    strongest_domain_code: str
    domain_summaries: List[DomainTimelineSummary]
    timeline_windows: List[TemporalForecastWindow]
