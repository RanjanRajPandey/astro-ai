from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class EvidenceItemModel(BaseModel):
    factor: str
    category: str = Field(
        ...,
        description="HOUSE_AND_LORD, KARAKA_STRENGTH, DIVISIONAL_VARGA, YOGA_OR_DOSHA, or DASHA_AND_GOCHAR",
    )
    observation: str
    rule_reference: str
    finding: str = Field(..., description="FAVORABLE, CHALLENGING, or NEUTRAL")
    weight: float


class EvidenceGenerationRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    question_text: Optional[str] = Field(
        default="What are my strongest career, authority, and financial periods?",
    )
    question_category: Optional[str] = None
    target_datetime_utc: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class EvidenceGenerationResponse(BaseModel):
    framework_version: str
    question_text: str
    question_category: str
    primary_houses: List[int]
    required_vargas: List[str]
    total_evidence_count: int
    favorable_count: int
    challenging_count: int
    neutral_count: int
    evidence_items: List[EvidenceItemModel]
    factors_considered: List[str]
    time_windows_summary: List[str]
