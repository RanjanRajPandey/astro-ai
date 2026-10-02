from datetime import date, time
from aspects.drishti import (
    calculate_all_aspects,
    compute_sphuta_drishti_virupas,
    get_parashari_house_aspect,
)
from models.aspects import AspectCalculationRequest


def test_parashari_special_and_pada_aspects() -> None:
    # Sun: only 7th house is Full (60 Virupas); 4/8 = 45, 5/9 = 30, 3/10 = 15
    sun_7 = get_parashari_house_aspect("Sun", 7)
    assert sun_7 is not None
    assert sun_7[0] == "7TH_FULL"
    assert sun_7[1] is True
    assert sun_7[4] == 60.0

    sun_4 = get_parashari_house_aspect("Sun", 4)
    assert sun_4 is not None
    assert sun_4[1] is False
    assert sun_4[3] == "3/4"
    assert sun_4[4] == 45.0

    # Mars: 4th, 7th, 8th are Full (60 Virupas)
    for offset in (4, 7, 8):
        mars_asp = get_parashari_house_aspect("Mars", offset)
        assert mars_asp is not None
        assert mars_asp[1] is True
        assert mars_asp[4] == 60.0

    # Jupiter: 5th, 7th, 9th are Full (60 Virupas)
    for offset in (5, 7, 9):
        jup_asp = get_parashari_house_aspect("Jupiter", offset)
        assert jup_asp is not None
        assert jup_asp[1] is True
        assert jup_asp[4] == 60.0

    # Saturn: 3rd, 7th, 10th are Full (60 Virupas)
    for offset in (3, 7, 10):
        sat_asp = get_parashari_house_aspect("Saturn", offset)
        assert sat_asp is not None
        assert sat_asp[1] is True
        assert sat_asp[4] == 60.0

    # Rahu/Ketu trinal toggle
    rahu_5_on = get_parashari_house_aspect("Rahu", 5, rahu_ketu_trinal_aspects=True)
    assert rahu_5_on is not None and rahu_5_on[1] is True and rahu_5_on[4] == 60.0

    rahu_5_off = get_parashari_house_aspect("Rahu", 5, rahu_ketu_trinal_aspects=False)
    assert rahu_5_off is not None and rahu_5_off[1] is False and rahu_5_off[4] == 30.0


def test_sphuta_drishti_continuous_virupas() -> None:
    # Exact 180 deg opposition is 60 Virupas for any planet
    assert compute_sphuta_drishti_virupas("Sun", 10.0, 190.0) == 60.0
    # Less than 30 deg is 0 Virupas
    assert compute_sphuta_drishti_virupas("Venus", 100.0, 115.0) == 0.0
    # Mars at 90 deg separation (4th aspect) -> 45 base + 15 special = 60 Virupas
    assert compute_sphuta_drishti_virupas("Mars", 0.0, 90.0) == 60.0
    # Jupiter at 120 deg separation (5th aspect) -> 30 base + 30 special = 60 Virupas
    assert compute_sphuta_drishti_virupas("Jupiter", 0.0, 120.0) == 60.0
    # Saturn at 270 deg separation (10th aspect) -> 15 base + 45 special = 60 Virupas
    assert compute_sphuta_drishti_virupas("Saturn", 0.0, 270.0) == 60.0


def test_full_aspect_calculation_and_rahu_ketu_opposition() -> None:
    req = AspectCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(12, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
        rahu_ketu_trinal_aspects=True,
        include_pada_drishti=True,
    )
    resp = calculate_all_aspects(req)

    # 9 planets * 7 aspect offsets (3, 4, 5, 7, 8, 9, 10) = 63 house aspect entries
    assert len(resp.house_aspects) == 63

    # Full house aspects:
    # Sun(1) + Moon(1) + Mercury(1) + Venus(1) + Mars(3) + Jupiter(3) + Saturn(3) + Rahu(3) + Ketu(3) = 19
    full_house_aspects = [ha for ha in resp.house_aspects if ha.is_full_aspect]
    assert len(full_house_aspects) == 19

    # Rahu and Ketu are always 180 deg apart in opposite houses -> must appear in mutual_relationships
    rk_mutual = [
        m
        for m in resp.mutual_relationships
        if {m.planet_a.upper(), m.planet_b.upper()} == {"RAHU", "KETU"}
    ]
    assert len(rk_mutual) == 1
    assert rk_mutual[0].relationship_type == "MUTUAL_7TH_OPPOSITION"
    assert rk_mutual[0].combined_virupa_strength == 120.0
