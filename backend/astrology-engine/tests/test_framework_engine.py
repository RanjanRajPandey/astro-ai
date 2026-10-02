from fastapi.testclient import TestClient
from api.main import app
from frameworks.classifier import (
    FRAMEWORK_CATALOG,
    classify_question_and_load_framework,
)
from models.frameworks import QuestionClassificationRequest

client = TestClient(app)


def test_question_classification_across_domains():
    assert len(FRAMEWORK_CATALOG) == 9

    res_career = classify_question_and_load_framework(
        QuestionClassificationRequest(question_text="When will I get a job promotion and leadership role?")
    )
    assert res_career.primary_category == "CAREER_AND_PROFESSION"
    assert "D10" in res_career.active_framework.required_vargas
    assert 10 in res_career.active_framework.primary_houses

    res_marriage = classify_question_and_load_framework(
        QuestionClassificationRequest(question_text="When will I get married and how is my spouse compatibility in D9?")
    )
    assert res_marriage.primary_category == "MARRIAGE_AND_RELATIONSHIPS"
    assert "D9" in res_marriage.active_framework.required_vargas
    assert 7 in res_marriage.active_framework.primary_houses

    res_wealth = classify_question_and_load_framework(
        QuestionClassificationRequest(question_text="How is my financial wealth, stock investment, and income?")
    )
    assert res_wealth.primary_category == "WEALTH_AND_FINANCE"
    assert "D2" in res_wealth.active_framework.required_vargas

    res_foreign = classify_question_and_load_framework(
        QuestionClassificationRequest(question_text="Will I settle abroad or get an overseas visa?")
    )
    assert res_foreign.primary_category == "FOREIGN_TRAVEL_AND_SETTLEMENT"
    assert 12 in res_foreign.active_framework.primary_houses


def test_framework_fastapi_endpoint():
    resp = client.post(
        "/engine/frameworks/classify",
        json={"question_text": "Can I buy a new house and luxury vehicle this year?"},
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["primary_category"] == "PROPERTY_AND_VEHICLES"
    assert len(body["all_frameworks"]) == 9
    assert "D4" in body["active_framework"]["required_vargas"]
    assert "D16" in body["active_framework"]["required_vargas"]
