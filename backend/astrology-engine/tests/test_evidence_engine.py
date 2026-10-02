from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from evidence.evaluator import generate_astrological_evidence
from models.evidence import EvidenceGenerationRequest

client = TestClient(app)


def test_evidence_generation_career():
    req = EvidenceGenerationRequest(
        date_of_birth=date(1990, 1, 1),
        time_of_birth=time(12, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        question_text="Will I get a promotion and achieve high status in my career?",
    )
    res = generate_astrological_evidence(req)
    assert res.question_category == "CAREER_AND_PROFESSION"
    assert res.total_evidence_count > 0
    assert len(res.evidence_items) == res.total_evidence_count
    assert res.favorable_count + res.challenging_count + res.neutral_count == res.total_evidence_count
    assert any(item.category == "HOUSE_AND_LORD" for item in res.evidence_items)
    assert any(item.category == "KARAKA_STRENGTH" for item in res.evidence_items)
    assert any(item.category == "DIVISIONAL_VARGA" for item in res.evidence_items)
    for item in res.evidence_items:
        assert item.finding in ("FAVORABLE", "CHALLENGING", "NEUTRAL")
        assert item.weight > 0
        assert len(item.rule_reference) > 0


def test_evidence_generation_marriage():
    req = EvidenceGenerationRequest(
        date_of_birth=date(1995, 5, 15),
        time_of_birth=time(8, 30, 0),
        latitude=19.0760,
        longitude=72.8777,
        timezone_id="Asia/Kolkata",
        question_text="When will I get married and how will my relationship with spouse be?",
    )
    res = generate_astrological_evidence(req)
    assert res.question_category == "MARRIAGE_AND_RELATIONSHIPS"
    assert "D9" in res.required_vargas
    assert 7 in res.primary_houses
    assert res.total_evidence_count >= 5


def test_evidence_fastapi_endpoint():
    resp = client.post(
        "/engine/evidence/generate",
        json={
            "date_of_birth": "1990-01-01",
            "time_of_birth": "12:00:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
            "question_text": "How will my health and longevity be?",
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["question_category"] == "HEALTH_AND_LONGEVITY"
    assert body["total_evidence_count"] > 0
    assert len(body["evidence_items"]) > 0
    assert len(body["factors_considered"]) > 0
