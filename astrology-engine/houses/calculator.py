"""
Deterministic 12-House (Bhava) Engine.
Implements Whole Sign Houses (Rashi = Bhava) paired with Sripati Bhava Chalit cusps,
House Lordships, Occupants, Parashari Graha Drishti on Houses, Classifications, and Baseline House Strength.
"""

from dataclasses import dataclass
from typing import Dict, List
from ascendant.calculator import AscendantDetails, calculate_ascendant_details
from astronomy.ephemeris import calculate_sidereal_ascendant
from planets.calculator import CalculatedPlanet, calculate_planetary_positions
from rules.vedic_constants import (
    ZODIAC_SIGNS,
    SANSKRIT_SIGN_NAMES,
    SIGN_LORDS,
)

HOUSE_SIGNIFICATIONS: Dict[int, List[str]] = {
    1: ["Self", "Physical Constitution", "Vitality", "Personality", "General Headway"],
    2: ["Accumulated Wealth (Dhana)", "Family (Kutumba)", "Speech (Vak)", "Early Education"],
    3: ["Courage (Parakrama)", "Younger Siblings", "Communication", "Short Journeys", "Initiative"],
    4: ["Mother (Matru)", "Inner Peace (Sukha)", "Home & Property", "Vehicles", "Foundational Education"],
    5: ["Intelligence (Dhi)", "Children (Putra)", "Past-Life Merit (Purva Punya)", "Speculation", "Creativity"],
    6: ["Obstacles", "Debts (Rina)", "Health Challenges (Roga)", "Competitors (Shatru)", "Daily Service"],
    7: ["Marriage & Spouse (Kalatra)", "Business Partnerships", "Public Relations", "Contracts"],
    8: ["Longevity (Ayur)", "Sudden Transformations", "Research & Occult", "Joint Assets / Inheritance"],
    9: ["Fortune (Bhagya)", "Dharma & Righteousness", "Father / Guru", "Higher Wisdom", "Long Journeys"],
    10: ["Career & Profession (Karma)", "Public Status", "Authority", "Social Contribution"],
    11: ["Gains & Income (Labha)", "Fulfillment of Desires", "Elder Siblings", "Professional Networks"],
    12: ["Liberation (Moksha)", "Expenses (Vyaya)", "Foreign Residence", "Sleep", "Spiritual Retreat"],
}

PURUSHARTHA_BY_HOUSE: Dict[int, str] = {
    1: "DHARMA", 5: "DHARMA", 9: "DHARMA",
    2: "ARTHA", 6: "ARTHA", 10: "ARTHA",
    3: "KAMA", 7: "KAMA", 11: "KAMA",
    4: "MOKSHA", 8: "MOKSHA", 12: "MOKSHA",
}

NATURAL_BENEFICS = {"Jupiter", "Venus", "Mercury", "Moon"}
NATURAL_MALEFICS = {"Sun", "Mars", "Saturn", "Rahu", "Ketu"}

DIGNITY_POINTS: Dict[str, float] = {
    "EXALTED": 95.0,
    "MOOLATRIKONA": 85.0,
    "OWN_SIGN": 80.0,
    "GREAT_FRIEND": 72.0,
    "FRIEND": 65.0,
    "NEUTRAL": 55.0,
    "ENEMY": 42.0,
    "GREAT_ENEMY": 32.0,
    "DEBILITATED": 25.0,
}


@dataclass(frozen=True)
class HouseAspectInfo:
    source_planet: str
    source_house: int
    source_sign: str
    aspect_house_distance: int
    aspect_type: str
    rule: str


@dataclass(frozen=True)
class CalculatedHouse:
    house_number: int
    sign: str
    sanskrit_sign: str
    sign_index: int
    lord_planet: str
    lord_placed_in_house: int
    lord_placed_in_sign: str
    lord_dignity: str
    degree_cusp: float
    sripati_cusp_longitude: float
    sripati_start_longitude: float
    sripati_end_longitude: float
    occupants: List[str]
    chalit_occupants: List[str]
    aspects_received: List[HouseAspectInfo]
    purushartha: str
    classifications: List[str]
    significations: List[str]
    baseline_strength_score: float
    strength_grade: str


@dataclass(frozen=True)
class HouseCalculationResult:
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    house_system: str
    ascendant: AscendantDetails
    houses: List[CalculatedHouse]


def _circular_midpoint(lon1: float, lon2: float) -> float:
    """Compute forward ecliptic midpoint from lon1 to lon2 in [0, 360)."""
    span = (lon2 - lon1) % 360.0
    return (lon1 + span / 2.0) % 360.0


def _is_longitude_in_arc(lon: float, start_lon: float, end_lon: float) -> bool:
    """Check if longitude lies within forward arc [start_lon, end_lon)."""
    span = (end_lon - start_lon) % 360.0
    offset = (lon - start_lon) % 360.0
    return offset < span


def _get_house_classifications(house_num: int) -> List[str]:
    classes: List[str] = []
    if house_num in {1, 4, 7, 10}:
        classes.append("KENDRA")
    if house_num in {1, 5, 9}:
        classes.append("TRIKONA")
    if house_num in {3, 6, 10, 11}:
        classes.append("UPACHAYA")
    if house_num in {6, 8, 12}:
        classes.append("DUSTHANA")
    if house_num in {2, 7}:
        classes.append("MARAKA")
    if house_num in {2, 5, 8, 11}:
        classes.append("PANAPARA")
    if house_num in {3, 6, 9, 12}:
        classes.append("APOKLIMA")
    return classes


def _get_parashari_aspects_from_planet(
    planet_name: str, rahu_ketu_trinal: bool = False
) -> List[ tuple[int, str, str] ]:
    """
    Return list of (house_distance, aspect_type, rule_description) cast by a planet.
    In Parashari Jyotish (BPHS Ch. 26):
    - Every planet aspects the 7th house from itself.
    - Mars has special full aspects on 4th and 8th houses.
    - Jupiter has special full aspects on 5th and 9th houses.
    - Saturn has special full aspects on 3rd and 10th houses.
    """
    aspects = [
        (7, "7TH_FULL", f"{planet_name} casts universal 7th house full aspect (BPHS Ch. 26).")
    ]
    if planet_name == "Mars":
        aspects.append((4, "MARS_4TH_SPECIAL", "Mars casts special 4th house full aspect (Vishesha Drishti)."))
        aspects.append((8, "MARS_8TH_SPECIAL", "Mars casts special 8th house full aspect (Vishesha Drishti)."))
    elif planet_name == "Jupiter":
        aspects.append((5, "JUPITER_5TH_SPECIAL", "Jupiter casts special 5th house trinal aspect (Vishesha Drishti)."))
        aspects.append((9, "JUPITER_9TH_SPECIAL", "Jupiter casts special 9th house trinal aspect (Vishesha Drishti)."))
    elif planet_name == "Saturn":
        aspects.append((3, "SATURN_3RD_SPECIAL", "Saturn casts special 3rd house full aspect (Vishesha Drishti)."))
        aspects.append((10, "SATURN_10TH_SPECIAL", "Saturn casts special 10th house full aspect (Vishesha Drishti)."))
    elif planet_name in ("Rahu", "Ketu") and rahu_ketu_trinal:
        aspects.append((5, "NODE_5TH_TRINAL", f"{planet_name} casts configured 5th house nodal aspect."))
        aspects.append((9, "NODE_9TH_TRINAL", f"{planet_name} casts configured 9th house nodal aspect."))
    return aspects


def calculate_houses_and_ascendant(
    jd_ut: float,
    latitude: float,
    longitude: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
    rahu_ketu_trinal_aspects: bool = False,
) -> HouseCalculationResult:
    """
    Deterministically compute Ascendant, Special Lagnas, and all 12 Houses (Whole Sign + Sripati Cusps).
    """
    planetary_res = calculate_planetary_positions(
        jd_ut=jd_ut,
        latitude=latitude,
        longitude=longitude,
        ayanamsha_type=ayanamsha_type,
        node_type=node_type,
    )
    asc_lon, sripati_cusps = calculate_sidereal_ascendant(
        jd_ut=jd_ut,
        latitude=latitude,
        longitude=longitude,
        ayanamsha_type=ayanamsha_type,
    )

    planets_by_name: Dict[str, CalculatedPlanet] = {
        p.planet: p for p in planetary_res.planets
    }

    asc_details = calculate_ascendant_details(asc_lon, planets_by_name)
    asc_sign_idx_0 = asc_details.sign_index - 1

    # Compute Sripati Bhava Arambha (start) and Bhava Virama (end) midpoints between adjacent cusps
    sripati_starts: List[float] = []
    sripati_ends: List[float] = []
    for i in range(12):
        prev_cusp = sripati_cusps[(i - 1) % 12]
        curr_cusp = sripati_cusps[i]
        next_cusp = sripati_cusps[(i + 1) % 12]
        sripati_starts.append(_circular_midpoint(prev_cusp, curr_cusp))
        sripati_ends.append(_circular_midpoint(curr_cusp, next_cusp))

    # Pre-collect Parashari aspects received by each house (1..12)
    aspects_by_house: Dict[int, List[HouseAspectInfo]] = {h: [] for h in range(1, 13)}
    for p in planetary_res.planets:
        for dist, asp_type, rule_str in _get_parashari_aspects_from_planet(
            p.planet, rahu_ketu_trinal=rahu_ketu_trinal_aspects
        ):
            target_house = ((p.house - 1 + (dist - 1)) % 12) + 1
            aspects_by_house[target_house].append(
                HouseAspectInfo(
                    source_planet=p.planet,
                    source_house=p.house,
                    source_sign=p.sign,
                    aspect_house_distance=dist,
                    aspect_type=asp_type,
                    rule=rule_str,
                )
            )

    houses: List[CalculatedHouse] = []
    for h_num in range(1, 13):
        idx_0 = h_num - 1
        house_sign_idx_0 = (asc_sign_idx_0 + idx_0) % 12
        sign_name = ZODIAC_SIGNS[house_sign_idx_0]
        lord_name = SIGN_LORDS[sign_name]
        lord_obj = planets_by_name[lord_name]

        occupants = [p.planet for p in planetary_res.planets if p.house == h_num]
        chalit_occupants = [
            p.planet
            for p in planetary_res.planets
            if _is_longitude_in_arc(p.longitude, sripati_starts[idx_0], sripati_ends[idx_0])
        ]

        # Transparent Baseline House Strength Calculation (0..100 scale)
        score = DIGNITY_POINTS.get(lord_obj.dignity, 55.0)
        # Lord placement modifier: Kendra/Trikona (+8), Dusthana 6/8/12 (-8 unless Own Sign/Exalted)
        if lord_obj.house in {1, 4, 5, 7, 9, 10}:
            score += 8.0
        elif lord_obj.house in {6, 8, 12} and not (lord_obj.is_own_sign or lord_obj.is_exalted):
            score -= 8.0

        # Occupant modifier
        for occ in occupants:
            if occ == lord_name:
                score += 6.0  # Bhavesha in Swabhava
            if occ in NATURAL_BENEFICS:
                score += 5.0
            elif occ in NATURAL_MALEFICS:
                score -= 3.0

        # Aspect modifier
        for asp in aspects_by_house[h_num]:
            if asp.source_planet == lord_name:
                score += 6.0  # House aspected by its own lord
            elif asp.source_planet in NATURAL_BENEFICS:
                score += 4.0
            elif asp.source_planet in NATURAL_MALEFICS:
                score -= 2.5

        score_clamped = round(max(10.0, min(100.0, score)), 2)
        if score_clamped >= 75.0:
            grade = "STRONG"
        elif score_clamped >= 55.0:
            grade = "MODERATE"
        else:
            grade = "CHALLENGED"

        cusp_lon = sripati_cusps[idx_0]
        houses.append(
            CalculatedHouse(
                house_number=h_num,
                sign=sign_name,
                sanskrit_sign=SANSKRIT_SIGN_NAMES[sign_name],
                sign_index=house_sign_idx_0 + 1,
                lord_planet=lord_name,
                lord_placed_in_house=lord_obj.house,
                lord_placed_in_sign=lord_obj.sign,
                lord_dignity=lord_obj.dignity,
                degree_cusp=round(cusp_lon % 30.0, 6),
                sripati_cusp_longitude=round(cusp_lon, 6),
                sripati_start_longitude=round(sripati_starts[idx_0], 6),
                sripati_end_longitude=round(sripati_ends[idx_0], 6),
                occupants=occupants,
                chalit_occupants=chalit_occupants,
                aspects_received=aspects_by_house[h_num],
                purushartha=PURUSHARTHA_BY_HOUSE[h_num],
                classifications=_get_house_classifications(h_num),
                significations=HOUSE_SIGNIFICATIONS[h_num],
                baseline_strength_score=score_clamped,
                strength_grade=grade,
            )
        )

    return HouseCalculationResult(
        julian_day_ut=planetary_res.julian_day_ut,
        ayanamsha_type=planetary_res.ayanamsha_type,
        ayanamsha_value=planetary_res.ayanamsha_value,
        house_system="WHOLE_SIGN_WITH_SRIPATI",
        ascendant=asc_details,
        houses=houses,
    )
