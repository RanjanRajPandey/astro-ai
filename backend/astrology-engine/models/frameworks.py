from typing import List, Optional
from pydantic import BaseModel, Field


class FrameworkChecklistRule(BaseModel):
    rule_code: str
    factor_category: str = Field(
        ...,
        description="HOUSE_AND_LORD, KARAKA_STRENGTH, DIVISIONAL_VARGA, YOGA_OR_DOSHA, or DASHA_AND_GOCHAR",
    )
    description: str
    classical_reference: str
    weight: float


class AnalysisFrameworkDefinition(BaseModel):
    category_code: str
    title: str
    sanskrit_title: str
    description: str
    primary_houses: List[int]
    secondary_houses: List[int]
    required_vargas: List[str]
    naisargika_karakas: List[str]
    special_lagnas: List[str]
    key_yogas_to_check: List[str]
    checklist_rules: List[FrameworkChecklistRule]


class QuestionClassificationRequest(BaseModel):
    question_text: Optional[str] = Field(
        default="What are my strongest career and financial periods?",
        description="Natural language astrological question from the user",
    )


class QuestionClassificationResponse(BaseModel):
    framework_version: str
    question_text: str
    primary_category: str
    secondary_category: Optional[str] = None
    confidence_score: float
    matched_keywords: List[str]
    active_framework: AnalysisFrameworkDefinition
    all_frameworks: List[AnalysisFrameworkDefinition]
