from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from nakshatra.calculator import (
    NAKSHATRA_CATALOG,
    calculate_nakshatras,
    compute_single_nakshatra_placement,
)
from timezone.resolver import resolve_birth_timestamp

client = TestClient(app)


def test_nakshatra_catalog_and_pada_navamsha_mapping():
    assert len(NAKSHATRA_CATALOG) == 27
    # 0° Aries -> Ashwini (1) Pada 1 -> Aries Navamsha
    p1 = compute_single_nakshatra_placement("Sun", 1.0, moon_nakshatra_index_1=1, relationship_to_ruler="FRIEND")
    assert p1.nakshatra_name == "Ashwini"
    assert p1.nakshatra_index == 1
    assert p1.pada == 1
    assert p1.pada_navamsha_sign == "Aries"
    assert p1.tara_name_from_moon == "JANMA"

    # 14° Aries -> Bharani (2) Pada 1 (since 13°20' to 16°40' is Bharani Pada 1 -> Leo Navamsha)
    p2 = compute_single_nakshatra_placement("Venus", 14.0, moon_nakshatra_index_1=1, relationship_to_ruler="OWN_STAR")
    assert p2.nakshatra_name == "Bharani"
    assert p2.nakshatra_index == 2
    assert p2.pada == 1
    assert p2.pada_navamsha_sign == "Leo"
    assert p2.tara_name_from_moon == "SAMPAT"


def test_full_nakshatra_calculation_and_endpoint():
    resolved = resolve_birth_timestamp(
        birth_date=date(1990, 5, 15),
        birth_time=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
    )
    res = calculate_nakshatras(
        jd_ut=resolved.julian_day_ut,
        latitude=28.6139,
        longitude=77.2090,
    )

    # Ascendant + 9 Grahas = 10 placements
    assert len(res.placements) == 10
    assert abs((res.moon_elapsed_fraction + res.moon_remaining_fraction) - 1.0) < 1e-7

    response = client.post(
        "/api/v1/nakshatra/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
        },
    )
    assert response.status_code == 200
    payload = response.json()
    assert len(payload["placements"]) == 10
    moon_pl = next(p for p in payload["placements"] if p["body_name"] == "Moon")
    assert moon_pl["tara_name_from_moon"] == "JANMA"
    assert moon_pl["nakshatra_name"] == payload["janma_nakshatra"]
