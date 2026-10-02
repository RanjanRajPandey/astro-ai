from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from models.temporal import TemporalAnalysisRequest
from temporal.synthesizer import calculate_temporal_analysis

client = TestClient(app)


def test_temporal_analysis_synthesis_and_confluence_windows():
    req = TemporalAnalysisRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        anchor_datetime_utc="2026-09-30T12:00:00Z",
        window_count=6,
    )
    res = calculate_temporal_analysis(req)
    assert res.natal_ascendant_sign == "Virgo"
    assert res.natal_moon_sign == "Capricorn"
    assert res.window_count == 6
    assert len(res.domain_summaries) == 5
    assert len(res.timeline_windows) == 6
    assert res.best_overall_window_label != ""
    assert res.strongest_domain_code in {
        "CAREER_AND_AUTHORITY",
        "WEALTH_AND_ASSETS",
        "MARRIAGE_AND_PARTNERSHIPS",
        "HEALTH_AND_VITALITY",
        "WISDOM_AND_SPIRITUALITY",
    }

    for w in res.timeline_windows:
        assert len(w.domain_evaluations) == 5
        assert w.mahadasha_lord != ""
        assert w.antardasha_lord != ""
        assert w.pratyantardasha_lord != ""
        for ev in w.domain_evaluations:
            assert 0.0 <= ev.overall_confluence_score <= 100.0
            assert ev.window_classification in (
                "HIGH_OPPORTUNITY",
                "FAVORABLE_GROWTH",
                "STEADY_CONSOLIDATION",
                "CAUTION_AND_REMEDY",
            )
            assert len(ev.supporting_factors) >= 1


def test_temporal_fastapi_endpoint():
    resp = client.post(
        "/engine/temporal/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
            "anchor_datetime_utc": "2026-09-30T12:00:00Z",
            "window_count": 6,
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["window_count"] == 6
    assert len(body["domain_summaries"]) == 5
    assert len(body["timeline_windows"]) == 6
