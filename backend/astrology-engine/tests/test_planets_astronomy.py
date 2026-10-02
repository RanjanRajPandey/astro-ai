from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from planets.calculator import (
    calculate_planetary_positions,
    get_natural_relationship,
    get_temporal_relationship,
    get_compound_relationship,
)
from timezone.resolver import resolve_birth_timestamp

client = TestClient(app)


def test_panchadha_maitri_5_fold_relationships():
    # Sun & Mars: Natural Friends. If Mars is in 2nd house from Sun (e.g., Aries=0 -> Taurus=1), Temporal Friend -> GREAT_FRIEND
    assert get_natural_relationship("Sun", "Mars") == "FRIEND"
    assert get_temporal_relationship(0, 1) == "FRIEND"
    assert get_compound_relationship("FRIEND", "FRIEND") == "GREAT_FRIEND"

    # Sun & Saturn: Natural Enemies. If Saturn is in 7th house from Sun (Aries=0 -> Libra=6), Temporal Enemy -> GREAT_ENEMY
    assert get_natural_relationship("Sun", "Saturn") == "ENEMY"
    assert get_temporal_relationship(0, 6) == "ENEMY"
    assert get_compound_relationship("ENEMY", "ENEMY") == "GREAT_ENEMY"

    # Sun & Mercury: Natural Neutral. If Mercury is in 2nd house (Temporal Friend) -> FRIEND
    assert get_natural_relationship("Sun", "Mercury") == "NEUTRAL"
    assert get_compound_relationship("NEUTRAL", "FRIEND") == "FRIEND"


def test_sidereal_planetary_invariants_and_golden_reference():
    # Golden Reference Case: New Delhi (28.6139N, 77.2090E), 1990-05-15 14:30:00 IST (09:00 UTC)
    resolved = resolve_birth_timestamp(
        birth_date=date(1990, 5, 15),
        birth_time=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
    )
    result = calculate_planetary_positions(
        jd_ut=resolved.julian_day_ut,
        latitude=28.6139,
        longitude=77.2090,
        ayanamsha_type="LAHIRI",
        node_type="MEAN_NODE",
    )

    assert len(result.planets) == 9
    assert 23.5 < result.ayanamsha_value < 24.0  # Lahiri Ayanamsha in May 1990 is ~23.729°
    assert result.ascendant_sign == "Virgo"  # 14:30 IST (~9h after 05:31 Taurus sunrise) -> Kanya (Virgo) Lagna

    planets_by_name = {p.planet: p for p in result.planets}

    # Verify Sun is in Taurus (Vrishabha) at ~0°46' (since May 15 is right after Vrishabha Sankranti)
    sun = planets_by_name["Sun"]
    assert sun.sign == "Taurus"
    assert sun.sanskrit_sign == "Vrishabha"
    assert sun.is_retrograde is False
    assert sun.is_combust is False
    assert sun.house == 9  # Taurus is 9th house (Trikona) from Virgo Lagna

    # Verify Moon is never retrograde
    moon = planets_by_name["Moon"]
    assert moon.is_retrograde is False
    assert 1 <= moon.nakshatra_index <= 27
    assert 1 <= moon.pada <= 4

    # Verify Rahu & Ketu exact 180-degree invariant and retrograde status
    rahu = planets_by_name["Rahu"]
    ketu = planets_by_name["Ketu"]
    assert rahu.is_retrograde is True
    assert ketu.is_retrograde is True
    assert rahu.is_combust is False
    assert ketu.is_combust is False
    expected_ketu_lon = round((rahu.longitude + 180.0) % 360.0, 6)
    assert abs(ketu.longitude - expected_ketu_lon) < 1e-5
    assert abs(ketu.degree_in_sign - rahu.degree_in_sign) < 1e-5

    # Verify Saturn in Capricorn (Own Sign / Swakshetra) in May 1990
    saturn = planets_by_name["Saturn"]
    assert saturn.sign == "Capricorn"
    assert saturn.is_own_sign is True
    assert saturn.dignity == "OWN_SIGN"


def test_planet_calculation_fastapi_endpoint():
    response = client.post(
        "/api/v1/planets/calculate",
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
    assert payload["ascendant_sign"] == "Virgo"
    assert len(payload["planets"]) == 9
    for p in payload["planets"]:
        assert 0.0 <= p["longitude"] < 360.0
        assert 0.0 <= p["degree_in_sign"] < 30.0
        assert 1 <= p["house"] <= 12
        assert 1 <= p["pada"] <= 4
        assert len(p["planetary_relationships"]) == 8
