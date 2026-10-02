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


def test_ranjan_raj_pandey_saturn_mahadasha_calculation():
    """Verify BPHS Vimshottari calculation for native Ranjan Raj Pandey.
    Born: 2004-08-22 18:05:00 IST in Kushinagar, UP.
    Moon in Vishakha (Lord: Jupiter) -> Birth Balance Jupiter Mahadasha -> Saturn Mahadasha active from 2015 to 2034.
    In 2026, native MUST be under Saturn Mahadasha and Moon Antardasha.
    """
    req = DashaCalculationRequest(
        date_of_birth=date(2004, 8, 22),
        time_of_birth=time(18, 5, 0),
        latitude=26.7402,
        longitude=83.8886,
        timezone_id="Asia/Kolkata",
        target_datetime_iso="2026-10-02T00:00:00Z",
    )
    res = calculate_vimshottari_dasha(req)

    # Janma Nakshatra is Vishakha, ruled by Jupiter
    assert res.janma_nakshatra == "Vishakha"
    assert res.birth_dasha_lord == "Jupiter"

    # Active stack in 2026 must be Saturn Mahadasha -> Moon Antardasha -> Rahu Pratyantardasha
    assert len(res.active_stack) == 5
    assert res.active_stack[0].level == 1
    assert res.active_stack[0].planet == "Saturn"
    assert res.active_stack[0].level_name == "MAHADASHA"

    assert res.active_stack[1].level == 2
    assert res.active_stack[1].planet == "Moon"
    assert res.active_stack[1].level_name == "ANTARDASHA"

    assert res.active_stack[2].level == 3
    assert res.active_stack[2].planet == "Rahu"
    assert res.active_stack[2].level_name == "PRATYANTARDASHA"

    # Saturn Mahadasha in the 9-cycle list must have is_currently_active=True
    saturn_maha = next(m for m in res.mahadashas if m.planet == "Saturn")
    assert saturn_maha.is_currently_active is True
    assert saturn_maha.start_date_time.startswith("2015")
    assert saturn_maha.end_date_time.startswith("2034")

    # Jupiter Mahadasha is the birth balance period
    jupiter_maha = res.mahadashas[0]
    assert jupiter_maha.planet == "Jupiter"
    assert jupiter_maha.is_birth_balance_period is True
    assert jupiter_maha.is_currently_active is False

