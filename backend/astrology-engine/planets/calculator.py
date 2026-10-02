"""
Deterministic Planetary Position, Dignity, Combustion, Nakshatra, and
Panchadha Maitri (5-Fold Planetary Relationship) Calculator.
"""

from dataclasses import dataclass
from typing import Dict, List, Optional
from astronomy.ephemeris import (
    calculate_raw_sidereal_bodies,
    calculate_sidereal_ascendant,
    get_ayanamsha_value,
)
from rules.vedic_constants import (
    ZODIAC_SIGNS,
    SANSKRIT_SIGN_NAMES,
    SIGN_LORDS,
    GRAHA_ORDER,
    NAKSHATRA_LIST,
    EXALTATION_RULES,
    DEBILITATION_RULES,
    MOOLATRIKONA_RULES,
    OWN_SIGNS,
    COMBUSTION_ORBS,
    NATURAL_FRIENDS,
    NATURAL_ENEMIES,
)


def format_sign_dms(degree_in_sign: float) -> str:
    """Format degree within sign [0..30) as DD° MM' SS.S\"."""
    deg_clamped = max(0.0, min(29.999999, degree_in_sign))
    d = int(deg_clamped)
    rem_min = (deg_clamped - d) * 60.0
    m = int(rem_min)
    s = round((rem_min - m) * 60.0, 1)
    if s >= 60.0:
        s = 0.0
        m += 1
    if m >= 60:
        m = 0
        d += 1
    return f"{d:02d}° {m:02d}' {s:04.1f}\""


def shortest_angular_distance(lon1: float, lon2: float) -> float:
    """Return shortest angular separation in [0, 180] degrees on the ecliptic circle."""
    diff = abs((lon1 - lon2) % 360.0)
    return 360.0 - diff if diff > 180.0 else diff


def get_natural_relationship(planet: str, other_planet: str) -> str:
    """Return Naisargika Maitri: FRIEND, ENEMY, or NEUTRAL."""
    if planet == other_planet:
        return "SELF"
    if other_planet in NATURAL_FRIENDS.get(planet, set()):
        return "FRIEND"
    if other_planet in NATURAL_ENEMIES.get(planet, set()):
        return "ENEMY"
    return "NEUTRAL"


def get_temporal_relationship(sign_index_1: int, sign_index_2: int) -> str:
    """
    Return Tatkalika Maitri per BPHS Ch. 3:
    Planets in 2nd, 3rd, 4th, 10th, 11th, 12th signs from a planet (1-indexed inclusive count)
    are Temporal Friends (MITRA); planets in 1st, 5th, 6th, 7th, 8th, 9th are Temporal Enemies (SHATRU).
    """
    house_distance = ((sign_index_2 - sign_index_1) % 12) + 1
    if house_distance in {2, 3, 4, 10, 11, 12}:
        return "FRIEND"
    return "ENEMY"


def get_compound_relationship(natural_rel: str, temporal_rel: str) -> str:
    """
    Combine Naisargika (Natural) and Tatkalika (Temporal) into Panchadha Maitri (5-fold):
    - FRIEND + FRIEND   -> GREAT_FRIEND (Adhi Mitra)
    - NEUTRAL + FRIEND  -> FRIEND (Mitra)
    - ENEMY + FRIEND    -> NEUTRAL (Sama)
    - FRIEND + ENEMY    -> NEUTRAL (Sama)
    - NEUTRAL + ENEMY   -> ENEMY (Shatru)
    - ENEMY + ENEMY     -> GREAT_ENEMY (Adhi Shatru)
    """
    if natural_rel == "FRIEND" and temporal_rel == "FRIEND":
        return "GREAT_FRIEND"
    if natural_rel == "NEUTRAL" and temporal_rel == "FRIEND":
        return "FRIEND"
    if (natural_rel == "ENEMY" and temporal_rel == "FRIEND") or (
        natural_rel == "FRIEND" and temporal_rel == "ENEMY"
    ):
        return "NEUTRAL"
    if natural_rel == "NEUTRAL" and temporal_rel == "ENEMY":
        return "ENEMY"
    if natural_rel == "ENEMY" and temporal_rel == "ENEMY":
        return "GREAT_ENEMY"
    return "NEUTRAL"


@dataclass(frozen=True)
class CalculatedPlanet:
    planet: str
    longitude: float
    latitude: float
    speed_longitude: float
    sign: str
    sanskrit_sign: str
    sign_index: int  # 1..12
    sign_lord: str
    degree_in_sign: float
    degree_dms: str
    house: int  # 1..12 relative to Ascendant
    nakshatra: str
    nakshatra_index: int  # 1..27
    nakshatra_lord: str
    pada: int  # 1..4
    is_retrograde: bool
    is_combust: bool
    angular_distance_from_sun: Optional[float]
    is_exalted: bool
    is_debilitated: bool
    is_moolatrikona: bool
    is_own_sign: bool
    dignity: str
    dispositor_relationship: str
    planetary_relationships: Dict[str, Dict[str, str]]


@dataclass(frozen=True)
class PlanetaryCalculationResult:
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    node_type: str
    ascendant_longitude: float
    ascendant_sign: str
    ascendant_sign_index: int
    ascendant_degree_in_sign: float
    ascendant_dms: str
    planets: List[CalculatedPlanet]


def calculate_planetary_positions(
    jd_ut: float,
    latitude: float,
    longitude: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
) -> PlanetaryCalculationResult:
    """
    Deterministically compute all 9 Grahas with full Vedic astrological attributes.
    """
    ayanamsha_val = get_ayanamsha_value(jd_ut, ayanamsha_type)
    asc_lon, _ = calculate_sidereal_ascendant(jd_ut, latitude, longitude, ayanamsha_type)
    asc_sign_idx = int(asc_lon // 30.0)  # 0..11
    asc_sign = ZODIAC_SIGNS[asc_sign_idx]
    asc_deg_in_sign = asc_lon % 30.0

    raw_bodies = calculate_raw_sidereal_bodies(jd_ut, ayanamsha_type, node_type)
    sun_lon = raw_bodies["Sun"].longitude

    # First pass: determine sign index (0..11) for each planet for Tatkalika Maitri
    planet_sign_indices: Dict[str, int] = {
        name: int(body.longitude // 30.0) % 12 for name, body in raw_bodies.items()
    }

    calculated_planets: List[CalculatedPlanet] = []

    for name in GRAHA_ORDER:
        body = raw_bodies[name]
        lon = body.longitude
        sign_idx_0 = planet_sign_indices[name]
        sign_name = ZODIAC_SIGNS[sign_idx_0]
        sign_lord = SIGN_LORDS[sign_name]
        deg_in_sign = lon % 30.0

        # Whole-sign house relative to Ascendant (1..12)
        house_num = ((sign_idx_0 - asc_sign_idx) % 12) + 1

        # Nakshatra (27 divisions of 13°20' = 13.3333333333°) and Pada (4 divisions of 3°20')
        nak_span = 360.0 / 27.0
        pada_span = nak_span / 4.0
        nak_idx_0 = min(26, int(lon // nak_span))
        nak_name, nak_lord = NAKSHATRA_LIST[nak_idx_0]
        offset_in_nak = lon - (nak_idx_0 * nak_span)
        pada = min(4, int(offset_in_nak // pada_span) + 1)

        # Retrograde status: Sun and Moon never retrograde; Rahu/Ketu always retrograde in Vedic convention
        if name in ("Sun", "Moon"):
            is_retro = False
        elif name in ("Rahu", "Ketu"):
            is_retro = True
        else:
            is_retro = body.speed_longitude < 0.0

        # Combustion status (Asta)
        if name in COMBUSTION_ORBS:
            direct_orb, retro_orb = COMBUSTION_ORBS[name]
            orb = retro_orb if is_retro else direct_orb
            ang_dist = round(shortest_angular_distance(lon, sun_lon), 6)
            is_combust = ang_dist <= orb
        elif name == "Sun":
            ang_dist = 0.0
            is_combust = False
        else:
            ang_dist = round(shortest_angular_distance(lon, sun_lon), 6)
            is_combust = False

        # Compute 5-fold Panchadha Maitri with all other planets
        relationships: Dict[str, Dict[str, str]] = {}
        for other_name in GRAHA_ORDER:
            if other_name == name:
                continue
            nat_rel = get_natural_relationship(name, other_name)
            temp_rel = get_temporal_relationship(sign_idx_0, planet_sign_indices[other_name])
            comp_rel = get_compound_relationship(nat_rel, temp_rel)
            relationships[other_name] = {
                "natural": nat_rel,
                "temporal": temp_rel,
                "compound": comp_rel,
            }

        # Dispositor relationship
        if sign_lord == name:
            dispositor_rel = "OWN_SIGN"
        else:
            dispositor_rel = relationships[sign_lord]["compound"]

        # Dignity Evaluation (Priority: Exalted / Moolatrikona / Own Sign / Debilitated / Dispositor Compound)
        exalt_sign, _ = EXALTATION_RULES[name]
        debil_sign, _ = DEBILITATION_RULES[name]
        is_exalted = sign_name == exalt_sign
        is_debilitated = sign_name == debil_sign

        is_moolatrikona = False
        if name in MOOLATRIKONA_RULES:
            mt_sign, mt_min, mt_max = MOOLATRIKONA_RULES[name]
            if sign_name == mt_sign and mt_min <= deg_in_sign < mt_max:
                is_moolatrikona = True
                # Note: For Moon (Taurus 0-3 Exalted, 3-30 Moolatrikona) & Mercury (Virgo 0-15 Exalted, 15-20 Moolatrikona)
                if name in ("Moon", "Mercury"):
                    is_exalted = False

        is_own = sign_name in OWN_SIGNS.get(name, set())

        if is_exalted:
            dignity = "EXALTED"
        elif is_moolatrikona:
            dignity = "MOOLATRIKONA"
        elif is_own:
            dignity = "OWN_SIGN"
        elif is_debilitated:
            dignity = "DEBILITATED"
        else:
            dignity = dispositor_rel

        calculated_planets.append(
            CalculatedPlanet(
                planet=name,
                longitude=round(lon, 6),
                latitude=round(body.latitude, 6),
                speed_longitude=round(body.speed_longitude, 6),
                sign=sign_name,
                sanskrit_sign=SANSKRIT_SIGN_NAMES[sign_name],
                sign_index=sign_idx_0 + 1,
                sign_lord=sign_lord,
                degree_in_sign=round(deg_in_sign, 6),
                degree_dms=format_sign_dms(deg_in_sign),
                house=house_num,
                nakshatra=nak_name,
                nakshatra_index=nak_idx_0 + 1,
                nakshatra_lord=nak_lord,
                pada=pada,
                is_retrograde=is_retro,
                is_combust=is_combust,
                angular_distance_from_sun=ang_dist,
                is_exalted=is_exalted,
                is_debilitated=is_debilitated,
                is_moolatrikona=is_moolatrikona,
                is_own_sign=is_own,
                dignity=dignity,
                dispositor_relationship=dispositor_rel,
                planetary_relationships=relationships,
            )
        )

    return PlanetaryCalculationResult(
        julian_day_ut=round(jd_ut, 8),
        ayanamsha_type=ayanamsha_type.upper(),
        ayanamsha_value=round(ayanamsha_val, 8),
        node_type=node_type.upper(),
        ascendant_longitude=round(asc_lon, 6),
        ascendant_sign=asc_sign,
        ascendant_sign_index=asc_sign_idx + 1,
        ascendant_degree_in_sign=round(asc_deg_in_sign, 6),
        ascendant_dms=format_sign_dms(asc_deg_in_sign),
        planets=calculated_planets,
    )
