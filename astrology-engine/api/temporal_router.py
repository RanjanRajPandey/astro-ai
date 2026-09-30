from fastapi import APIRouter, HTTPException
from models.temporal import TemporalAnalysisRequest, TemporalAnalysisResponse
from temporal.synthesizer import calculate_temporal_analysis

router = APIRouter(prefix="/engine/temporal", tags=["Temporal Analysis & Dasha-Gochar Confluence"])


@router.post("/calculate", response_model=TemporalAnalysisResponse)
def post_calculate_temporal_analysis(request: TemporalAnalysisRequest) -> TemporalAnalysisResponse:
    try:
        return calculate_temporal_analysis(request)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
