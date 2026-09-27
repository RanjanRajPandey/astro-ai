from datetime import date, time
from fastapi.testclient import TestClient

from api.main import app
from dashas.vimshottari import (
    DAYS_PER_YEAR,
    VIMSHOTTARI_SEQUENCE,
    VIMSHOTTARI_TOTAL_YEARS,
    VIMSHOTTARI_YEARS,
    calculate_vimshottari_dasha,
)
from models.dashas import DashaCalculationRequest

client = TestClient(app)


def test_vimshottari_120_year_constants_and_proportions():
    assert len(VIMSHOTTARI_SEQUENCE) == 9
    assert sum(VIMSHOTTARI_YEARS.values()) == VIMSHOTTARI_TOTAL_YEARS == 120.0
    assert DAYS_PER_YEAR == 365.2425


def test_5_level_vimshottari_dasha_calculation_and_endpoint():
    req = DashaCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        target_datetime_iso="2026-09-28T00:00:00Z",
    )
    res = calculate_vimshottari_dasha(req)

    # 9 Mahadashas in canonical order starting from birth_dasha_lord
    assert len(res.mahadashas) == 9
    assert res.mahadashas[0].planet == res.birth_dasha_lord
    assert res.mahadashas[0].is_birth_balance_period is True
    assert abs(res.mahadashas[0].duration_years - res.birth_balance_years) < 1e-4

    # Check that full Mahadashas (indices 1..8) have exact BPHS durations and 9 Antardashas each
    for maha in res.mahadashas[1:]:
        expected_years = VIMSHOTTARI_YEARS[maha.planet]
        assert abs(maha.duration_years - expected_years) < 1e-4
        assert len(maha.sub_periods) == 9
        # First Antardasha of any full Mahadasha is the Mahadasha planet itself
        assert maha.sub_periods[0].planet == maha.planet
        # Sum of 9 Antardasha years equals the Mahadasha years
        antar_sum = sum(a.duration_years for a in maha.sub_periods)
        assert abs(antar_sum - expected_years) < 1e-3
        # Each full Antardasha has 9 Pratyantardashas (Level 3)
        for antar in maha.sub_periods:
            assert len(antar.sub_periods) == 9
            assert antar.sub_periods[0].planet == antar.planet

    # Verify all 5 levels in active_stack (L1 Mahadasha -> L2 Antardasha -> L3 Pratyantar -> L4 Sookshma -> L5 Prana)
    assert len(res.active_stack) == 5
    assert [item.level for item in res.active_stack] == [1, 2, 3, 4, 5]
    assert [item.level_name for item in res.active_stack] == [
        "MAHADASHA",
        "ANTARDASHA",
        "PRATYANTARDASHA",
        "SOOKSHMA_DASHA",
        "PRANA_DASHA",
    ]
    assert len(res.active_sookshma_periods) == 9
    assert len(res.active_prana_periods) == 9

    # FastAPI endpoint check
    http_res = client.post(
        "/api/v1/dashas/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
            "target_datetime_iso": "2026-09-28T00:00:00Z",
        },
    )
    assert http_res.status_code == 200
    body = http_res.json()
    assert len(body["active_stack"]) == 5
    assert len(body["mahadashas"]) == 9
