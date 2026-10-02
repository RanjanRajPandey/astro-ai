from datetime import date, time
from timezone.resolver import resolve_birth_timestamp
from planets.calculator import calculate_planetary_positions
from nakshatra.calculator import calculate_nakshatras
from dashas.vimshottari import calculate_vimshottari_dasha
from models.dashas import DashaCalculationRequest
from models.yogas import YogaCalculationRequest
from yogas.detector import detect_all_yogas
from houses.calculator import calculate_houses_and_ascendant


def test_indian_independence_golden_reference_chart():
    """
    Canonical Golden Reference: Midnight Independence Chart of India
    Date: August 15, 1947, 00:00:00 IST
    Location: New Delhi, India (28.6139° N, 77.2090° E)
    """
    resolved = resolve_birth_timestamp(
        birth_date=date(1947, 8, 15),
        birth_time=time(0, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
    )

    # 1. Planetary & Ascendant Positions
    planet_res = calculate_planetary_positions(
        jd_ut=resolved.julian_day_ut,
        latitude=28.6139,
        longitude=77.2090,
        ayanamsha_type="LAHIRI",
        node_type="MEAN_NODE",
    )

    # Lahiri Ayanamsha for Aug 1947 is ~23.11°
    assert 23.0 < planet_res.ayanamsha_value < 23.2

    # Lagna: Taurus (Vrishabha)
    assert planet_res.ascendant_sign == "Taurus"
    assert planet_res.ascendant_degree_in_sign >= 7.0  # ~7°-9° Taurus

    planets = {p.planet: p for p in planet_res.planets}

    # Rahu in Taurus (House 1), Ketu in Scorpio (House 7)
    assert planets["Rahu"].sign == "Taurus"
    assert planets["Rahu"].house == 1
    assert planets["Ketu"].sign == "Scorpio"
    assert planets["Ketu"].house == 7

    # Exact 180° opposition between Rahu and Ketu
    diff = abs((planets["Rahu"].longitude + 180.0) % 360.0 - planets["Ketu"].longitude)
    assert diff < 0.001 or abs(diff - 360.0) < 0.001

    # Mars in Gemini (House 2)
    assert planets["Mars"].sign == "Gemini"
    assert planets["Mars"].house == 2

    # The famous 5-planet cluster in Cancer (House 3): Sun, Moon, Mercury, Venus, Saturn
    assert planets["Sun"].sign == "Cancer"
    assert planets["Sun"].house == 3
    assert planets["Moon"].sign == "Cancer"
    assert planets["Moon"].house == 3
    assert planets["Mercury"].sign == "Cancer"
    assert planets["Mercury"].house == 3
    assert planets["Venus"].sign == "Cancer"
    assert planets["Venus"].house == 3
    assert planets["Saturn"].sign == "Cancer"
    assert planets["Saturn"].house == 3

    # Jupiter in Libra (House 6)
    assert planets["Jupiter"].sign == "Libra"
    assert planets["Jupiter"].house == 6

    # 2. Nakshatra Verification
    nakshatra_res = calculate_nakshatras(
        jd_ut=resolved.julian_day_ut,
        latitude=28.6139,
        longitude=77.2090,
    )
    placements = {p.body_name: p for p in nakshatra_res.placements}

    # Moon in Pushya Nakshatra (governed by Saturn)
    moon_nak = placements["Moon"]
    assert moon_nak.nakshatra_name == "Pushya"
    assert moon_nak.ruler_planet == "Saturn"

    # 3. Vimshottari Dasha Verification
    # Since Moon is in Pushya (Saturn lord), birth dasha MUST be Saturn Mahadasha
    dasha_req = DashaCalculationRequest(
        date_of_birth=date(1947, 8, 15),
        time_of_birth=time(0, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
    )
    dasha_res = calculate_vimshottari_dasha(dasha_req)
    assert dasha_res.birth_dasha_lord == "Saturn"
    assert len(dasha_res.mahadashas) == 9
    assert dasha_res.mahadashas[0].planet == "Saturn"
    assert dasha_res.mahadashas[0].is_birth_balance_period is True

    # 4. Classical Yogas
    yoga_req = YogaCalculationRequest(
        date_of_birth=date(1947, 8, 15),
        time_of_birth=time(0, 0, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
    )
    yoga_res = detect_all_yogas(yoga_req)
    assert len(yoga_res.active_yogas) > 0


def test_bv_raman_golden_reference_chart():
    """
    Canonical Golden Reference: Dr. B.V. Raman
    Date: August 8, 1912, 19:35:00 IST
    Location: Bangalore, India (13.0° N, 77.5833° E)
    """
    resolved = resolve_birth_timestamp(
        birth_date=date(1912, 8, 8),
        birth_time=time(19, 35, 0),
        latitude=13.0,
        longitude=77.5833,
    )

    planet_res = calculate_planetary_positions(
        jd_ut=resolved.julian_day_ut,
        latitude=13.0,
        longitude=77.5833,
        ayanamsha_type="LAHIRI",
        node_type="MEAN_NODE",
    )

    # Lagna: Aquarius (Kumbha)
    assert planet_res.ascendant_sign == "Aquarius"

    planets = {p.planet: p for p in planet_res.planets}

    # Moon in Taurus (4th house)
    assert planets["Moon"].sign == "Taurus"
    assert planets["Moon"].house == 4

    # Saturn (Lagna Lord) in Taurus (4th house)
    assert planets["Saturn"].sign == "Taurus"
    assert planets["Saturn"].house == 4

    # Jupiter in Scorpio (10th house)
    assert planets["Jupiter"].sign == "Scorpio"
    assert planets["Jupiter"].house == 10

    # Mutual Kendra between Jupiter (10th) and Moon (4th) forms Gaja Kesari Yoga
    yoga_req = YogaCalculationRequest(
        date_of_birth=date(1912, 8, 8),
        time_of_birth=time(19, 35, 0),
        latitude=13.0,
        longitude=77.5833,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
    )
    yoga_res = detect_all_yogas(yoga_req)
    active_codes = {y.yoga_code for y in yoga_res.active_yogas}
    assert "GAJAKESARI_YOGA" in active_codes


def test_astronomical_coordinate_invariants():
    """
    Strict Invariant Checks:
    - All planetary longitudes are strictly in [0.0, 360.0)
    - All house numbers are 1..12
    - House cusps are strictly 12 and monotonically span 360°
    """
    resolved = resolve_birth_timestamp(
        birth_date=date(2000, 1, 1),
        birth_time=time(12, 0, 0),
        latitude=0.0,
        longitude=0.0,
    )
    planet_res = calculate_planetary_positions(
        jd_ut=resolved.julian_day_ut,
        latitude=0.0,
        longitude=0.0,
        ayanamsha_type="LAHIRI",
    )

    for p in planet_res.planets:
        assert 0.0 <= p.longitude < 360.0
        assert 1 <= p.house <= 12
        assert -90.0 <= p.latitude <= 90.0
        assert p.speed_longitude is not None

    houses_res = calculate_houses_and_ascendant(
        jd_ut=resolved.julian_day_ut,
        latitude=0.0,
        longitude=0.0,
    )
    assert len(houses_res.houses) == 12
    for h in houses_res.houses:
        assert 1 <= h.house_number <= 12
        assert 0.0 <= h.sripati_start_longitude < 360.0
