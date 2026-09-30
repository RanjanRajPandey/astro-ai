"""
Deterministic Planetary Transits (Gochar), Vedha Obstruction, Sade Sati, Dhaiya,
Tara Bala, and Double Transit (Jupiter + Saturn) Activation Engine.
"""

from datetime import datetime, timezone
from typing import Dict, List, Optional, Set, Tuple
import swisseph as swe

from astronomy.ephemeris import calculate_raw_sidereal_bodies, calculate_sidereal_ascendant
from models.transits import (
    DoubleTransitHouseEntry,
    SadeSatiStatus,
    TransitCalculationRequest,
    TransitCalculationResponse,
    TransitPlanetEntry,
)
from nakshatra.calculator import NAKSHATRA_CATALOG, TARA_BALA_NAMES
from planets.calculator import calculate_planetary_positions, format_sign_dms
from rules.vedic_constants import (
    GRAHA_ORDER,
    SANSKRIT_SIGN_NAMES,
    ZODIAC_SIGNS,
)
from timezone.resolver import resolve_birth_timestamp


# Classical Gochar Benefic Houses from Natal Moon (Phaladeepika Ch. 26 & Brihat Samhita)
GOCHAR_BENEFIC_HOUSES: Dict[str, Set[int]] = {
    "Sun": {3, 6, 10, 11},
    "Moon": {1, 3, 6, 7, 10, 11},
    "Mars": {3, 6, 11},
    "Mercury": {2, 4, 6, 8, 10, 11},
    "Jupiter": {2, 5, 7, 9, 11},
    "Venus": {1, 2, 3, 4, 5, 8, 9, 11, 12},
    "Saturn": {3, 6, 11},
    "Rahu": {3, 6, 11},
    "Ketu": {3, 6, 11},
}

# Classical Vedha (Obstruction) Points: maps planet -> {benefic_house: vedha_house}
GOCHAR_VEDHA_MAP: Dict[str, Dict[int, int]] = {
    "Sun": {3: 9, 6: 12, 10: 4, 11: 5},
    "Moon": {1: 5, 3: 9, 6: 12, 7: 2, 10: 4, 11: 8},
    "Mars": {3: 12, 6: 9, 11: 5},
    "Mercury": {2: 5, 4: 3, 6: 9, 8: 1, 10: 8, 11: 12},
    "Jupiter": {2: 12, 5: 4, 7: 3, 9: 10, 11: 8},
    "Venus": {1: 8, 2: 7, 3: 1, 4: 10, 5: 9, 8: 5, 9: 11, 11: 6, 12: 3},
    "Saturn": {3: 12, 6: 9, 11: 5},
    "Rahu": {3: 12, 6: 9, 11: 5},
    "Ketu": {3: 12, 6: 9, 11: 5},
}

# Classical Father-Son Exceptions to Vedha: Sun-Saturn and Moon-Mercury do not obstruct each other
VEDHA_EXCEPTIONS: Set[Tuple[str, str]] = {
    ("Sun", "Saturn"),
    ("Saturn", "Sun"),
    ("Moon", "Mercury"),
    ("Mercury", "Moon"),
}

HOUSE_DOMAIN_TITLES: Dict[int, str] = {
    1: "Self, Vitality & New Beginnings (Tanu Bhava)",
    2: "Wealth, Family & Speech (Dhana Bhava)",
    3: "Courage, Initiative & Siblings (Sahaja Bhava)",
    4: "Home, Property & Inner Peace (Sukha Bhava)",
    5: "Intelligence, Creativity & Children (Putra Bhava)",
    6: "Service, Health & Overcoming Obstacles (Ari Bhava)",
    7: "Marriage, Partnerships & Public Ties (Yuvati Bhava)",
    8: "Transformation, Research & Joint Assets (Randhra Bhava)",
    9: "Fortune, Dharma & Higher Wisdom (Bhagya Bhava)",
    10: "Career, Authority & Public Status (Karma Bhava)",
    11: "Gains, Income & Aspirations (Labha Bhava)",
    12: "Moksha, Foreign Lands & Retreat (Vyaya Bhava)",
}


def _parse_transit_utc_datetime(iso_str: Optional[str]) -> datetime:
    if not iso_str or not iso_str.strip():
        return datetime.now(timezone.utc).replace(microsecond=0)
    cleaned = iso_str.strip()
    if cleaned.endswith("Z"):
        cleaned = cleaned[:-1] + "+00:00"
    dt = datetime.fromisoformat(cleaned)
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    else:
        dt = dt.astimezone(timezone.utc)
    return dt


def _datetime_utc_to_jd(dt_utc: datetime) -> float:
    hour_dec = (
        dt_utc.hour
        + (dt_utc.minute / 60.0)
        + ((dt_utc.second + dt_utc.microsecond / 1_000_000.0) / 3600.0)
    )
    return float(swe.julday(dt_utc.year, dt_utc.month, dt_utc.day, hour_dec))


def _evaluate_sade_sati(
    saturn_sign: str, saturn_house_from_moon: int, saturn_house_from_lagna: int, natal_moon_sign: str
) -> SadeSatiStatus:
    if saturn_house_from_moon == 12:
        return SadeSatiStatus(
            sade_sati_active=True,
            dhaiya_active=False,
            phase="RISING_12TH",
            saturn_transit_sign=saturn_sign,
            saturn_house_from_moon=12,
            saturn_house_from_lagna=saturn_house_from_lagna,
            description=(
                f"Rising Phase (1st Dhaiya) of Sade Sati: Saturn is transiting {saturn_sign} "
                f"(12th house from Natal Moon in {natal_moon_sign}), Restructuring foundations and expenses."
            ),
        )
    if saturn_house_from_moon == 1:
        return SadeSatiStatus(
            sade_sati_active=True,
            dhaiya_active=False,
            phase="PEAK_JANMA_1ST",
            saturn_transit_sign=saturn_sign,
            saturn_house_from_moon=1,
            saturn_house_from_lagna=saturn_house_from_lagna,
            description=(
                f"Peak Phase (Janma Shani) of Sade Sati: Saturn is transiting {saturn_sign} "
                f"directly over Natal Moon ({natal_moon_sign}), demanding discipline, resilience, and emotional maturity."
            ),
        )
    if saturn_house_from_moon == 2:
        return SadeSatiStatus(
            sade_sati_active=True,
            dhaiya_active=False,
            phase="SETTING_2ND",
            saturn_transit_sign=saturn_sign,
            saturn_house_from_moon=2,
            saturn_house_from_lagna=saturn_house_from_lagna,
            description=(
                f"Setting Phase (Final Dhaiya) of Sade Sati: Saturn is transiting {saturn_sign} "
                f"(2nd house from Natal Moon in {natal_moon_sign}), consolidating financial and family responsibilities."
            ),
        )
    if saturn_house_from_moon == 4:
        return SadeSatiStatus(
            sade_sati_active=False,
            dhaiya_active=True,
            phase="DHAIYA_KANTAKA_4TH",
            saturn_transit_sign=saturn_sign,
            saturn_house_from_moon=4,
            saturn_house_from_lagna=saturn_house_from_lagna,
            description=(
                f"Kantaka Shani (4th House Dhaiya): Saturn is transiting {saturn_sign} "
                f"(4th from Natal Moon in {natal_moon_sign}), activating domestic and career restructuring."
            ),
        )
    if saturn_house_from_moon == 8:
        return SadeSatiStatus(
            sade_sati_active=False,
            dhaiya_active=True,
            phase="DHAIYA_ASHTAMA_8TH",
            saturn_transit_sign=saturn_sign,
            saturn_house_from_moon=8,
            saturn_house_from_lagna=saturn_house_from_lagna,
            description=(
                f"Ashtama Shani (8th House Dhaiya): Saturn is transiting {saturn_sign} "
                f"(8th from Natal Moon in {natal_moon_sign}), requiring patience and careful health/financial stewardship."
            ),
        )

    return SadeSatiStatus(
        sade_sati_active=False,
        dhaiya_active=False,
        phase="NONE",
        saturn_transit_sign=saturn_sign,
        saturn_house_from_moon=saturn_house_from_moon,
        saturn_house_from_lagna=saturn_house_from_lagna,
        description=(
            f"No Sade Sati or Dhaiya active: Saturn is transiting {saturn_sign} "
            f"(House {saturn_house_from_moon} from Natal Moon in {natal_moon_sign})."
        ),
    )


def calculate_gochar_transits(request: TransitCalculationRequest) -> TransitCalculationResponse:
    """
    Compute complete sidereal planetary transits (Gochar), Vedha obstructions,
    Tara Bala, Sade Sati / Dhaiya status, and Double Transit (Jupiter + Saturn) house activations.
    """
    resolved_birth = resolve_birth_timestamp(
        birth_date=request.date_of_birth,
        birth_time=request.time_of_birth,
        latitude=request.latitude,
        longitude=request.longitude,
        explicit_timezone_id=request.timezone_id,
    )

    natal_calc = calculate_planetary_positions(
        jd_ut=resolved_birth.julian_day_ut,
        latitude=request.latitude,
        longitude=request.longitude,
        ayanamsha_type=request.ayanamsha_type,
        node_type=request.node_type,
    )
    natal_asc_sign_idx = natal_calc.ascendant_sign_index - 1  # 0..11
    natal_asc_sign = natal_calc.ascendant_sign
    natal_by_name = {p.planet: p for p in natal_calc.planets}
    natal_moon = natal_by_name["Moon"]
    natal_moon_sign_idx = natal_moon.sign_index - 1  # 0..11
    natal_moon_nak_idx = natal_moon.nakshatra_index  # 1..27

    # Resolve target transit UTC timestamp & Julian Day
    transit_dt_utc = _parse_transit_utc_datetime(request.transit_datetime_utc)
    transit_jd_ut = _datetime_utc_to_jd(transit_dt_utc)
    transit_iso = transit_dt_utc.strftime("%Y-%m-%dT%H:%M:%SZ")

    raw_transit_bodies = calculate_raw_sidereal_bodies(
        jd_ut=transit_jd_ut,
        ayanamsha_type=request.ayanamsha_type,
        node_type=request.node_type,
    )

    # First pass: compute house_from_moon and house_from_lagna for all 9 transiting Grahas
    transit_house_from_moon: Dict[str, int] = {}
    transit_house_from_lagna: Dict[str, int] = {}
    transit_sign_by_planet: Dict[str, str] = {}

    for planet_name in GRAHA_ORDER:
        body = raw_transit_bodies[planet_name]
        sign_idx_0 = int(body.longitude // 30.0) % 12
        transit_sign_by_planet[planet_name] = ZODIAC_SIGNS[sign_idx_0]
        transit_house_from_moon[planet_name] = ((sign_idx_0 - natal_moon_sign_idx) % 12) + 1
        transit_house_from_lagna[planet_name] = ((sign_idx_0 - natal_asc_sign_idx) % 12) + 1

    # Second pass: evaluate Gochar benefic status, Vedha obstructions, and Tara Bala
    planet_entries: List[TransitPlanetEntry] = []
    favorable_count = 0
    vedha_count = 0
    nak_span = 360.0 / 27.0
    pada_span = nak_span / 4.0

    for planet_name in GRAHA_ORDER:
        body = raw_transit_bodies[planet_name]
        lon = body.longitude
        sign_idx_0 = int(lon // 30.0) % 12
        t_sign = ZODIAC_SIGNS[sign_idx_0]
        t_sanskrit = SANSKRIT_SIGN_NAMES[t_sign]
        deg_in_sign = lon % 30.0
        dms_str = format_sign_dms(deg_in_sign)
        is_retro = True if planet_name in ("Rahu", "Ketu") else (body.speed_longitude < 0.0)

        nak_idx_0 = min(26, int(lon // nak_span))
        nak_meta = NAKSHATRA_CATALOG[nak_idx_0]
        deg_in_nak = lon - (nak_idx_0 * nak_span)
        pada = min(4, int(deg_in_nak // pada_span) + 1)

        # 9-fold Tara Bala relative to Natal Moon Nakshatra
        tara_num = (((nak_meta.index - natal_moon_nak_idx) % 27) % 9) + 1
        tara_name, tara_quality = TARA_BALA_NAMES[tara_num]
        is_tara_favorable = tara_quality == "SUPPORTING"

        h_moon = transit_house_from_moon[planet_name]
        h_lagna = transit_house_from_lagna[planet_name]

        benefic_houses = GOCHAR_BENEFIC_HOUSES.get(planet_name, set())
        is_benefic = h_moon in benefic_houses

        # Check Vedha obstruction if planet is in a benefic house from Moon
        vedha_obstructed = False
        vedha_obstructor: Optional[str] = None
        if is_benefic:
            vedha_house = GOCHAR_VEDHA_MAP.get(planet_name, {}).get(h_moon)
            if vedha_house is not None:
                for other_planet in GRAHA_ORDER:
                    if other_planet == planet_name:
                        continue
                    # Classical Gochar rule: Rahu/Ketu do not cause Vedha to classical 7 planets in standard BPHS/Phaladeepika tables,
                    # and father-son exceptions (Sun-Saturn, Moon-Mercury) do not obstruct each other
                    if (planet_name, other_planet) in VEDHA_EXCEPTIONS:
                        continue
                    if transit_house_from_moon[other_planet] == vedha_house:
                        vedha_obstructed = True
                        vedha_obstructor = other_planet
                        break

        if is_benefic and not vedha_obstructed:
            gochar_status = "FAVORABLE"
            favorable_count += 1
            summary = (
                f"{planet_name} transiting {t_sign} (H{h_moon} from Moon, H{h_lagna} from Lagna) "
                f"is in a classical auspicious Gochar house unobstructed by Vedha ({tara_name} Tara)."
            )
        elif is_benefic and vedha_obstructed:
            gochar_status = "VEDHA_OBSTRUCTED"
            vedha_count += 1
            summary = (
                f"{planet_name} transiting {t_sign} (H{h_moon} from Moon) is in an auspicious house, "
                f"but obstructed via Vedha by {vedha_obstructor} in H{transit_house_from_moon[vedha_obstructor]} from Moon."
            )
        else:
            gochar_status = "NEUTRAL_OR_CHALLENGING"
            summary = (
                f"{planet_name} transiting {t_sign} (H{h_moon} from Moon, H{h_lagna} from Lagna) "
                f"requires conscious effort; Nakshatra {nak_meta.name} P{pada} yields {tara_name} Tara ({tara_quality})."
            )

        natal_p = natal_by_name[planet_name]
        planet_entries.append(
            TransitPlanetEntry(
                planet=planet_name,
                natal_sign=natal_p.sign,
                natal_house_from_lagna=natal_p.house,
                transit_longitude=round(lon, 6),
                transit_sign=t_sign,
                transit_sanskrit_sign=t_sanskrit,
                transit_degree_dms=dms_str,
                transit_nakshatra=nak_meta.name,
                transit_pada=pada,
                is_retrograde=is_retro,
                house_from_moon=h_moon,
                house_from_lagna=h_lagna,
                is_benefic_from_moon=is_benefic,
                vedha_obstructed=vedha_obstructed,
                vedha_obstructor=vedha_obstructor,
                gochar_status=gochar_status,
                tara_bala_category=tara_name,
                is_tara_favorable=is_tara_favorable,
                classical_summary=summary,
            )
        )

    # Evaluate Sade Sati & Dhaiya
    sade_sati_status = _evaluate_sade_sati(
        saturn_sign=transit_sign_by_planet["Saturn"],
        saturn_house_from_moon=transit_house_from_moon["Saturn"],
        saturn_house_from_lagna=transit_house_from_lagna["Saturn"],
        natal_moon_sign=natal_moon.sign,
    )

    # Evaluate Double Transit (Jupiter + Saturn) activation on all 12 Natal Houses
    jup_h = transit_house_from_lagna["Jupiter"]
    jup_sign = transit_sign_by_planet["Jupiter"]
    sat_h = transit_house_from_lagna["Saturn"]
    sat_sign = transit_sign_by_planet["Saturn"]

    double_transit_houses: List[DoubleTransitHouseEntry] = []
    for house_num in range(1, 13):
        h_sign_idx = (natal_asc_sign_idx + house_num - 1) % 12
        h_sign = ZODIAC_SIGNS[h_sign_idx]
        h_sanskrit = SANSKRIT_SIGN_NAMES[h_sign]

        # Jupiter influences: 1st (occupation), 5th, 7th, 9th aspects
        jup_dist = ((house_num - jup_h) % 12) + 1
        jup_inf: Optional[str] = None
        if jup_dist == 1:
            jup_inf = f"Occupied by Jupiter in {jup_sign} (H{jup_h})"
        elif jup_dist in (5, 7, 9):
            jup_inf = f"{jup_dist}th Aspect from Jupiter in {jup_sign} (H{jup_h})"

        # Saturn influences: 1st (occupation), 3rd, 7th, 10th aspects
        sat_dist = ((house_num - sat_h) % 12) + 1
        sat_inf: Optional[str] = None
        if sat_dist == 1:
            sat_inf = f"Occupied by Saturn in {sat_sign} (H{sat_h})"
        elif sat_dist in (3, 7, 10):
            sat_inf = f"{sat_dist}th Aspect from Saturn in {sat_sign} (H{sat_h})"

        double_transit_houses.append(
            DoubleTransitHouseEntry(
                house_number=house_num,
                sign=h_sign,
                sanskrit_sign=h_sanskrit,
                domain_title=HOUSE_DOMAIN_TITLES[house_num],
                jupiter_influence=jup_inf,
                saturn_influence=sat_inf,
                is_activated=(jup_inf is not None and sat_inf is not None),
            )
        )

    return TransitCalculationResponse(
        natal_utc_datetime_iso=resolved_birth.utc_datetime_iso,
        transit_utc_datetime_iso=transit_iso,
        transit_julian_day_ut=round(transit_jd_ut, 6),
        natal_ascendant_sign=natal_asc_sign,
        natal_moon_sign=natal_moon.sign,
        natal_moon_nakshatra=natal_moon.nakshatra,
        ayanamsha_type=request.ayanamsha_type.upper(),
        favorable_transit_count=favorable_count,
        vedha_obstructed_count=vedha_count,
        sade_sati=sade_sati_status,
        double_transit_houses=double_transit_houses,
        planets=planet_entries,
    )
