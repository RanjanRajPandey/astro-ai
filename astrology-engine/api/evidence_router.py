from fastapi import APIRouter
from evidence.evaluator import generate_astrological_evidence
from models.evidence import EvidenceGenerationRequest, EvidenceGenerationResponse

router = APIRouter(
    prefix="/engine/evidence",
    tags=["Evidence Engine & Structured Observation Pipeline"],
)


@router.post("/generate", response_model=EvidenceGenerationResponse)
def post_generate_evidence(
    request: EvidenceGenerationRequest,
) -> EvidenceGenerationResponse:
    return generate_astrological_evidence(request)
