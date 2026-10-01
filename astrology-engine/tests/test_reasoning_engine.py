from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from models.reasoning import ReasoningSynthesisRequest
from reasoning.synthesizer import synthesize_astrological_reasoning

client = TestClient(app)


def test_reasoning_synthesis_career():
    req = ReasoningSynthesisRequest(
        date_of_birth=date(1990, 1, 1),
        time_of_birth=time(12, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        question_text="Will I attain high leadership status, promotion, and recognition in my career?",
    )
    res = synthesize_astrological_reasoning(req)
    assert res.question_category == "CAREER_AND_PROFESSION"
    assert len(res.reasoning_steps) == 5
    assert [s.step_order for s in res.reasoning_steps] == [1, 2, 3, 4, 5]
    assert [s.step_type for s in res.reasoning_steps] == [
        "NATAL_PROMISE",
        "DIVISIONAL_VALIDATION",
        "YOGA_CATALYSTS",
        "TEMPORAL_TRIGGER",
        "SYNTHESIS_AND_CONCLUSION",
    ]
    assert res.composite_score >= 0.0 and res.composite_score <= 100.0
    assert res.overall_verdict in ("FAVORABLE", "MODERATE_PROGRESS", "CHALLENGING")
    assert len(res.classical_remedies) >= 3
    for s in res.reasoning_steps:
        assert len(s.narrative) > 20
        assert len(s.shastra_citations) > 0
        assert s.confidence_score > 0.0


def test_reasoning_synthesis_marriage():
    req = ReasoningSynthesisRequest(
        date_of_birth=date(1995, 5, 15),
        time_of_birth=time(8, 30, 0),
        latitude=19.0760,
        longitude=72.8777,
        timezone_id="Asia/Kolkata",
        question_text="When is marriage likely to happen and how is relationship harmony?",
        question_category="MARRIAGE_AND_RELATIONSHIPS",
    )
    res = synthesize_astrological_reasoning(req)
    assert res.question_category == "MARRIAGE_AND_RELATIONSHIPS"
    assert len(res.reasoning_steps) == 5
    assert any("7th" in s.narrative or "D9" in s.narrative or "Marriage" in s.narrative or "Venus" in s.narrative for s in res.reasoning_steps)


def test_reasoning_fastapi_endpoint():
    resp = client.post(
        "/engine/reasoning/synthesize",
        json={
            "date_of_birth": "1992-07-24",
            "time_of_birth": "11:15:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
            "question_text": "What are my wealth accumulations and income potential?",
        },
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["question_category"] == "WEALTH_AND_FINANCE"
    assert len(data["reasoning_steps"]) == 5
    assert len(data["classical_remedies"]) > 0
