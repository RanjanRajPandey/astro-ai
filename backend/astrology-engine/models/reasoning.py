from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class ReasoningStepModel(BaseModel):
    step_order: int = Field(..., ge=1, le=5)
    step_type: str = Field(
        ...,
        description="NATAL_PROMISE, DIVISIONAL_VALIDATION, YOGA_CATALYSTS, TEMPORAL_TRIGGER, or SYNTHESIS_AND_CONCLUSION",
    )
    title: str
    verdict: str = Field(..., description="FAVORABLE, MODERATE, or CHALLENGING")
    confidence_score: float = Field(..., ge=0.0, le=1.0)
    narrative: str
    linked_factors: List[str] = Field(default_factory=list)
    shastra_citations: List[str] = Field(default_factory=list)


class ReasoningSynthesisRequest(BaseModel):
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    question_text: Optional[str] = Field(
        default="What are my strongest career, authority, and financial periods?",
    )
    question_category: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"


class ReasoningSynthesisResponse(BaseModel):
    framework_version: str
    question_text: str
    question_category: str
    primary_houses: List[int]
    overall_verdict: str = Field(..., description="FAVORABLE, MODERATE_PROGRESS, or CHALLENGING")
    composite_score: float = Field(..., ge=0.0, le=100.0)
    reasoning_steps: List[ReasoningStepModel]
    classical_remedies: List[str] = Field(default_factory=list)
