from fastapi import APIRouter
from frameworks.classifier import classify_question_and_load_framework
from models.frameworks import QuestionClassificationRequest, QuestionClassificationResponse

router = APIRouter(
    prefix="/engine/frameworks",
    tags=["Question Classifier & Domain Analysis Frameworks"],
)


@router.post("/classify", response_model=QuestionClassificationResponse)
def post_classify_question(
    request: QuestionClassificationRequest,
) -> QuestionClassificationResponse:
    return classify_question_and_load_framework(request)
