from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from models.transits import TransitCalculationRequest
from transits.gochar import (
    GOCHAR_BENEFIC_HOUSES,
    GOCHAR_VEDHA_MAP,
    VEDHA_EXCEPTIONS,
    _evaluate_sade_sati,
    calculate_gochar_transits,
)

client = TestClient(app)


def test_gochar_transits_calculation_and_double_transit():
    req = TransitCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        transit_datetime_utc="2026-09-30T12:00:00Z",
    )
    res = calculate_gochar_transits(req)
    assert res.natal_ascendant_sign == "Virgo"
    assert res.natal_moon_sign == "Capricorn"
    assert res.transit_utc_datetime_iso == "2026-09-30T12:00:00Z"
    assert len(res.planets) == 9
    assert len(res.double_transit_houses) == 12

    for p in res.planets:
        assert 1 <= p.house_from_moon <= 12
        assert 1 <= p.house_from_lagna <= 12
        assert p.gochar_status in ("FAVORABLE", "VEDHA_OBSTRUCTED", "NEUTRAL_OR_CHALLENGING")
        assert 1 <= p.transit_pada <= 4

    # At least one house must be activated by Jupiter + Saturn double transit
    activated = [h for h in res.double_transit_houses if h.is_activated]
    assert len(activated) >= 1


def test_sade_sati_and_dhaiya_phases():
    s12 = _evaluate_sade_sati("Sagittarius", 12, 4, "Capricorn")
    assert s12.sade_sati_active is True
    assert s12.phase == "RISING_12TH"

    s1 = _evaluate_sade_sati("Capricorn", 1, 5, "Capricorn")
    assert s1.sade_sati_active is True
    assert s1.phase == "PEAK_JANMA_1ST"

    s2 = _evaluate_sade_sati("Aquarius", 2, 6, "Capricorn")
    assert s2.sade_sati_active is True
    assert s2.phase == "SETTING_2ND"

    s4 = _evaluate_sade_sati("Aries", 4, 8, "Capricorn")
    assert s4.sade_sati_active is False
    assert s4.dhaiya_active is True
    assert s4.phase == "DHAIYA_KANTAKA_4TH"

    s8 = _evaluate_sade_sati("Leo", 8, 12, "Capricorn")
    assert s8.sade_sati_active is False
    assert s8.dhaiya_active is True
    assert s8.phase == "DHAIYA_ASHTAMA_8TH"


def test_vedha_tables_and_exceptions():
    assert ("Sun", "Saturn") in VEDHA_EXCEPTIONS
    assert ("Moon", "Mercury") in VEDHA_EXCEPTIONS
    for planet, benefic_set in GOCHAR_BENEFIC_HOUSES.items():
        vedha_map = GOCHAR_VEDHA_MAP[planet]
        assert set(vedha_map.keys()) == benefic_set


def test_transit_fastapi_endpoint():
    resp = client.post(
        "/engine/transits/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
            "transit_datetime_utc": "2026-09-30T12:00:00Z",
        },
    )
    assert resp.status_code == 200
    body = resp.json()
    assert body["natal_ascendant_sign"] == "Virgo"
    assert len(body["planets"]) == 9
    assert len(body["double_transit_houses"]) == 12
