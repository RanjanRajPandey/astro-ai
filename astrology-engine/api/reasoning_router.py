from fastapi import APIRouter
from models.reasoning import ReasoningSynthesisRequest, ReasoningSynthesisResponse
from reasoning.synthesizer import synthesize_astrological_reasoning

router = APIRouter(
    prefix="/engine/reasoning",
    tags=["Reasoning Engine & Synthesis Pipeline"],
)


@router.post("/synthesize", response_model=ReasoningSynthesisResponse)
def post_synthesize_reasoning(
    request: ReasoningSynthesisRequest,
) -> ReasoningSynthesisResponse:
    return synthesize_astrological_reasoning(request)
