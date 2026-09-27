from typing import Dict, List, Optional, Tuple

from models.divisional import (
    DivisionalCalculationRequest,
    DivisionalCalculationResponse,
    DivisionalChartModel,
    DivisionalHouseSummaryModel,
    DivisionalPlanetPlacementModel,
)
from planets.calculator import calculate_planetary_positions
from rules.vedic_constants import (
    DEBILITATION_RULES,
    EXALTATION_RULES,
    NATURAL_ENEMIES,
    NATURAL_FRIENDS,
    OWN_SIGNS,
    SANSKRIT_SIGN_NAMES,
    SIGN_LORDS,
    ZODIAC_SIGNS,
)
from timezone.resolver import resolve_birth_timestamp

SHODASHAVARGA_METADATA: List[Tuple[str, int, str, str, str]] = [
    ("D1", 1, "Rashi", "Rashi Chart (D1 - Natal Root)", "Physical body, general fortune, root synthesis"),
    ("D2", 2, "Hora", "Hora Chart (D2 - Wealth & Sustenance)", "Wealth, financial prosperity, family resources"),
    ("D3", 3, "Drekkana", "Drekkana Chart (D3 - Siblings & Courage)", "Siblings, courage, initiative, co-born"),
    ("D4", 4, "Chaturthamsha", "Chaturthamsha Chart (D4 - Fortune & Property)", "Fixed assets, home, real estate, bhagya"),
    ("D7", 7, "Saptamsha", "Saptamsha Chart (D7 - Progeny & Creativity)", "Children, progeny, creative legacy"),
    ("D9", 9, "Navamsha", "Navamsha Chart (D9 - Spouse, Dharma & Fruitage)", "Marriage, spouse, dharma, inner planetary strength"),
    ("D10", 10, "Dashamsha", "Dashamsha Chart (D10 - Career & Karma)", "Profession, career, authority, public status"),
    ("D12", 12, "Dwadashamsha", "Dwadashamsha Chart (D12 - Parents & Lineage)", "Parents, ancestral heritage, foundational conditioning"),
    ("D16", 16, "Shodashamsha", "Shodashamsha Chart (D16 - Vehicles & Comforts)", "Conveyances, vehicles, luxuries, inner sukha"),
    ("D20", 20, "Vimshamsha", "Vimshamsha Chart (D20 - Spiritual Upasana)", "Spiritual practice, devotion, sadhana"),
    ("D24", 24, "Chaturvimshamsha", "Chaturvimshamsha Chart (D24 - Learning & Vidya)", "Higher education, knowledge, scholarship"),
    ("D27", 27, "Saptavimshamsha", "Saptavimshamsha Chart (D27 - Vitality & Nature)", "Intrinsic strengths, weaknesses, stamina"),
    ("D30", 30, "Trimshamsha", "Trimshamsha Chart (D30 - Arishta & Challenges)", "Adversities, karmic tests, subconscious tendencies"),
    ("D40", 40, "Khavedamsha", "Khavedamsha Chart (D40 - Maternal Legacy)", "Maternal lineage, auspicious/inauspicious subtleties"),
    ("D45", 45, "Akshavedamsha", "Akshavedamsha Chart (D45 - Paternal & Ethical)", "Paternal lineage, moral integrity, fine conduct"),
    ("D60", 60, "Shashtiamsha", "Shashtiamsha Chart (D60 - Past-Life Root Karma)", "Sanchita/Prarabdha karma, subtle root differentiation"),
]

# Classical BPHS 60 Shashtiamsha (D60) Names & Quality (Odd-sign forward order; reversed in Even signs)
SHASHTIAMSHA_60_CATALOG: List[Tuple[str, str]] = [
    ("Ghora", "MALEFIC"),
    ("Rakshasa", "MALEFIC"),
    ("Deva", "BENEFIC"),
    ("Kubera", "BENEFIC"),
    ("Yaksha", "BENEFIC"),
    ("Kinnara", "BENEFIC"),
    ("Bhrashta", "MALEFIC"),
    ("Kulaghna", "MALEFIC"),
    ("Garala", "MALEFIC"),
    ("Vahni", "MALEFIC"),
    ("Maya", "MALEFIC"),
    ("Purishaka", "MALEFIC"),
    ("Apampati", "BENEFIC"),
    ("Marutwan", "BENEFIC"),
    ("Kaala", "MALEFIC"),
    ("Sarpa", "MALEFIC"),
    ("Amrita", "BENEFIC"),
    ("Indu", "BENEFIC"),
    ("Mridu", "BENEFIC"),
    ("Komala", "BENEFIC"),
    ("Heramba", "BENEFIC"),
    ("Brahma", "BENEFIC"),
    ("Vishnu", "BENEFIC"),
    ("Maheshwara", "BENEFIC"),
    ("Deva", "BENEFIC"),
    ("Ardra", "BENEFIC"),
    ("Kalinasa", "BENEFIC"),
    ("Kshitishwara", "BENEFIC"),
    ("Kamalakara", "BENEFIC"),
    ("Gulika", "MALEFIC"),
    ("Mrityu", "MALEFIC"),
    ("Kaala", "MALEFIC"),
    ("Davagni", "MALEFIC"),
    ("Ghora", "MALEFIC"),
    ("Yama", "MALEFIC"),
    ("Kantaka", "MALEFIC"),
    ("Sudha", "BENEFIC"),
    ("Amrita", "BENEFIC"),
    ("Purnachandra", "BENEFIC"),
    ("Vishadagdha", "MALEFIC"),
    ("Kulanasa", "MALEFIC"),
    ("Vamshakshaya", "MALEFIC"),
    ("Utpata", "MALEFIC"),
    ("Kaala", "MALEFIC"),
    ("Saumya", "BENEFIC"),
    ("Komala", "BENEFIC"),
    ("Sheetala", "BENEFIC"),
    ("Karaladamshtra", "MALEFIC"),
    ("Chandramukhi", "BENEFIC"),
    ("Praveena", "BENEFIC"),
    ("Kalapavaka", "MALEFIC"),
    ("Dandayudha", "MALEFIC"),
    ("Nirmala", "BENEFIC"),
    ("Saumya", "BENEFIC"),
    ("Krura", "MALEFIC"),
    ("Atisheetala", "BENEFIC"),
    ("Amrita", "BENEFIC"),
    ("Payodhi", "BENEFIC"),
    ("Bhramana", "MALEFIC"),
    ("Chandrarekha", "BENEFIC"),
]


def compute_varga_sign_and_part(longitude: float, division: int) -> Tuple[int, int]:
    """
    Computes the 0-indexed zodiac sign (0=Aries..11=Pisces) and 1-indexed part number (1..division)
    for any body at `longitude` [0, 360) in classical Parashari Shodashavarga `D{division}`.
    """
    norm_lon = longitude % 360.0
    s = int(norm_lon // 30.0) % 12
    deg_in_sign = norm_lon - (s * 30.0)
    is_odd_sign = (s % 2) == 0  # 0=Aries (1st sign, odd), 1=Taurus (2nd sign, even)
    modality = s % 3  # 0=Movable (Chara), 1=Fixed (Sthira), 2=Dual (Dwiswabhava)
    element = s % 4  # 0=Fire, 1=Earth, 2=Air, 3=Water

    if division == 1:
        return s, 1

    if division == 30:
        # BPHS Trimshamsha (D30) unequal degree spans
        if is_odd_sign:
            if deg_in_sign < 5.0:
                return 0, 1  # Aries (Mars)
            if deg_in_sign < 10.0:
                return 10, 2  # Aquarius (Saturn)
            if deg_in_sign < 18.0:
                return 8, 3  # Sagittarius (Jupiter)
            if deg_in_sign < 25.0:
                return 2, 4  # Gemini (Mercury)
            return 6, 5  # Libra (Venus)
        else:
            if deg_in_sign < 5.0:
                return 1, 1  # Taurus (Venus)
            if deg_in_sign < 12.0:
                return 5, 2  # Virgo (Mercury)
            if deg_in_sign < 20.0:
                return 11, 3  # Pisces (Jupiter)
            if deg_in_sign < 25.0:
                return 9, 4  # Capricorn (Saturn)
            return 7, 5  # Scorpio (Mars)

    span = 30.0 / float(division)
    p = min(division - 1, max(0, int(deg_in_sign // span)))
    part_num = p + 1

    if division == 2:
        # BPHS Hora: Odd signs -> Leo (4), Cancer (3); Even signs -> Cancer (3), Leo (4)
        if is_odd_sign:
            return (4 if p == 0 else 3), part_num
        return (3 if p == 0 else 4), part_num

    if division == 3:
        # Drekkana: 1st, 5th, 9th from sign
        return (s + p * 4) % 12, part_num

    if division == 4:
        # Chaturthamsha: 1st, 4th, 7th, 10th from sign
        return (s + p * 3) % 12, part_num

    if division == 7:
        # Saptamsha: Odd from same sign; Even from 7th sign
        base = s if is_odd_sign else (s + 6) % 12
        return (base + p) % 12, part_num

    if division == 9:
        # Navamsha: Fire->Aries(0), Earth->Capricorn(9), Air->Libra(6), Water->Cancer(3)
        base_map = {0: 0, 1: 9, 2: 6, 3: 3}
        return (base_map[element] + p) % 12, part_num

    if division == 10:
        # Dashamsha: Odd from same sign; Even from 9th sign
        base = s if is_odd_sign else (s + 8) % 12
        return (base + p) % 12, part_num

    if division == 12:
        # Dwadashamsha: always from same sign
        return (s + p) % 12, part_num

    if division == 16:
        # Shodashamsha: Movable->Aries(0), Fixed->Leo(4), Dual->Sagittarius(8)
        base_map = {0: 0, 1: 4, 2: 8}
        return (base_map[modality] + p) % 12, part_num

    if division == 20:
        # Vimshamsha: Movable->Aries(0), Fixed->Sagittarius(8), Dual->Leo(4)
        base_map = {0: 0, 1: 8, 2: 4}
        return (base_map[modality] + p) % 12, part_num

    if division == 24:
        # Chaturvimshamsha: Odd->Leo(4), Even->Cancer(3)
        base = 4 if is_odd_sign else 3
        return (base + p) % 12, part_num

    if division == 27:
        # Saptavimshamsha: Fire->Aries(0), Earth->Cancer(3), Air->Libra(6), Water->Capricorn(9)
        base_map = {0: 0, 1: 3, 2: 6, 3: 9}
        return (base_map[element] + p) % 12, part_num

    if division == 40:
        # Khavedamsha: Odd->Aries(0), Even->Libra(6)
        base = 0 if is_odd_sign else 6
        return (base + p) % 12, part_num

    if division == 45:
        # Akshavedamsha: Movable->Aries(0), Fixed->Leo(4), Dual->Sagittarius(8)
        base_map = {0: 0, 1: 4, 2: 8}
        return (base_map[modality] + p) % 12, part_num

    if division == 60:
        # Shashtiamsha: count from same sign
        return (s + p) % 12, part_num

    raise ValueError(f"Unsupported Shodashavarga division: D{division}")


def _evaluate_varga_dignity(
    planet: str,
    varga_sign_idx: int,
    all_varga_sign_indices: Dict[str, int],
) -> str:
    sign_name = ZODIAC_SIGNS[varga_sign_idx]
    if EXALTATION_RULES.get(planet, ("", 0.0))[0] == sign_name:
        return "EXALTED"
    if DEBILITATION_RULES.get(planet, ("", 0.0))[0] == sign_name:
        return "DEBILITATED"
    if sign_name in OWN_SIGNS.get(planet, set()):
        return "OWN_SIGN"

    dispositor = SIGN_LORDS[sign_name]
    if dispositor == planet:
        return "OWN_SIGN"

    if dispositor in NATURAL_FRIENDS.get(planet, set()):
        natural_score = 1
    elif dispositor in NATURAL_ENEMIES.get(planet, set()):
        natural_score = -1
    else:
        natural_score = 0

    p_sign_idx = all_varga_sign_indices[planet]
    disp_sign_idx = all_varga_sign_indices.get(dispositor, varga_sign_idx)
    house_dist = ((disp_sign_idx - p_sign_idx) % 12) + 1
    temporal_score = 1 if house_dist in (2, 3, 4, 10, 11, 12) else -1
    total = natural_score + temporal_score

    if total >= 2:
        return "GREAT_FRIEND"
    if total == 1:
        return "FRIEND"
    if total == 0:
        return "NEUTRAL"
    if total == -1:
        return "ENEMY"
    return "GREAT_ENEMY"


def _get_shashtiamsha_deity(longitude: float) -> Tuple[str, str]:
    norm_lon = longitude % 360.0
    s = int(norm_lon // 30.0) % 12
    deg_in_sign = norm_lon - (s * 30.0)
    p = min(59, max(0, int(deg_in_sign // 0.5)))
    is_odd_sign = (s % 2) == 0
    catalog_idx = p if is_odd_sign else (59 - p)
    return SHASHTIAMSHA_60_CATALOG[catalog_idx]


def calculate_shodashavarga(
    request: DivisionalCalculationRequest,
) -> DivisionalCalculationResponse:
    """
    Computes all 16 classical Brihat Parashara Hora Shastra (BPHS) Shodashavarga
    divisional charts (D1 through D60), or a specific requested Varga.
    """
    resolved_time = resolve_birth_timestamp(
        birth_date=request.date_of_birth,
        birth_time=request.time_of_birth,
        latitude=request.latitude,
        longitude=request.longitude,
        explicit_timezone_id=request.timezone_id,
    )

    planet_res = calculate_planetary_positions(
        jd_ut=resolved_time.julian_day_ut,
        latitude=request.latitude,
        longitude=request.longitude,
        ayanamsha_type=request.ayanamsha_type,
        node_type=request.node_type,
    )

    asc_d1_lon = float(planet_res.ascendant_longitude)
    asc_d1_sign_idx = int(planet_res.ascendant_sign_index) - 1  # 0-indexed

    requested_code = request.varga_code.strip().upper() if request.varga_code else None
    target_vargas = [
        meta
        for meta in SHODASHAVARGA_METADATA
        if requested_code is None or meta[0] == requested_code
    ]
    if not target_vargas:
        raise ValueError(f"Unsupported Varga code '{request.varga_code}'. Use D1..D60.")

    charts: List[DivisionalChartModel] = []
    d9_vargottama_list: List[str] = []

    for varga_code, division, sanskrit_name, title, domain_sig in target_vargas:
        asc_varga_sign_idx, _ = compute_varga_sign_and_part(asc_d1_lon, division)
        asc_varga_sign = ZODIAC_SIGNS[asc_varga_sign_idx]
        asc_varga_sanskrit = SANSKRIT_SIGN_NAMES[asc_varga_sign]
        asc_varga_lord = SIGN_LORDS[asc_varga_sign]
        is_asc_vargottama = asc_varga_sign_idx == asc_d1_sign_idx

        # First pass: compute varga sign indices for all 9 Grahas
        planet_varga_indices: Dict[str, int] = {}
        planet_parts: Dict[str, int] = {}
        for p in planet_res.planets:
            v_sign_idx, part_num = compute_varga_sign_and_part(float(p.longitude), division)
            planet_varga_indices[p.planet] = v_sign_idx
            planet_parts[p.planet] = part_num

        # Second pass: build planet placements and house occupants
        vargottama_planets: List[str] = []
        if is_asc_vargottama:
            vargottama_planets.append("Ascendant")

        house_occupants: Dict[int, List[str]] = {h: [] for h in range(1, 13)}
        planet_models: List[DivisionalPlanetPlacementModel] = []

        for p in planet_res.planets:
            v_sign_idx = planet_varga_indices[p.planet]
            v_sign = ZODIAC_SIGNS[v_sign_idx]
            v_sanskrit = SANSKRIT_SIGN_NAMES[v_sign]
            v_lord = SIGN_LORDS[v_sign]
            v_house = ((v_sign_idx - asc_varga_sign_idx) % 12) + 1
            house_occupants[v_house].append(p.planet)

            is_varg = v_sign == p.sign
            if is_varg:
                vargottama_planets.append(p.planet)

            dignity = (
                p.dignity
                if division == 1
                else _evaluate_varga_dignity(p.planet, v_sign_idx, planet_varga_indices)
            )

            d60_name: Optional[str] = None
            d60_qual: Optional[str] = None
            if division == 60:
                d60_name, d60_qual = _get_shashtiamsha_deity(float(p.longitude))

            planet_models.append(
                DivisionalPlanetPlacementModel(
                    planet=p.planet,
                    d1_longitude=round(float(p.longitude), 6),
                    d1_sign=p.sign,
                    d1_house=p.house,
                    varga_sign=v_sign,
                    varga_sanskrit_sign=v_sanskrit,
                    varga_sign_index=v_sign_idx + 1,
                    varga_sign_lord=v_lord,
                    varga_house=v_house,
                    part_number=planet_parts[p.planet],
                    is_vargottama=is_varg,
                    dignity_in_varga=dignity,
                    retrograde=p.is_retrograde,
                    combust=p.is_combust,
                    shashtiamsha_name=d60_name,
                    shashtiamsha_quality=d60_qual,
                )
            )

        if varga_code == "D9":
            d9_vargottama_list = list(vargottama_planets)

        houses_models: List[DivisionalHouseSummaryModel] = []
        for h_num in range(1, 13):
            h_sign_idx = (asc_varga_sign_idx + h_num - 1) % 12
            h_sign = ZODIAC_SIGNS[h_sign_idx]
            houses_models.append(
                DivisionalHouseSummaryModel(
                    house_number=h_num,
                    sign=h_sign,
                    sanskrit_sign=SANSKRIT_SIGN_NAMES[h_sign],
                    sign_index=h_sign_idx + 1,
                    lord_planet=SIGN_LORDS[h_sign],
                    occupants=house_occupants[h_num],
                )
            )

        charts.append(
            DivisionalChartModel(
                varga_code=varga_code,
                division_number=division,
                sanskrit_name=sanskrit_name,
                title=title,
                domain_signification=domain_sig,
                ascendant_sign=asc_varga_sign,
                ascendant_sanskrit_sign=asc_varga_sanskrit,
                ascendant_sign_index=asc_varga_sign_idx + 1,
                ascendant_lord=asc_varga_lord,
                is_ascendant_vargottama=is_asc_vargottama,
                vargottama_planets=vargottama_planets,
                planets=planet_models,
                houses=houses_models,
            )
        )

    return DivisionalCalculationResponse(
        utc_datetime_iso=resolved_time.utc_datetime_iso,
        julian_day_ut=planet_res.julian_day_ut,
        ayanamsha_type=planet_res.ayanamsha_type,
        ayanamsha_value=planet_res.ayanamsha_value,
        d1_ascendant_sign=planet_res.ascendant_sign,
        vargottama_Swapna_summary=d9_vargottama_list,
        charts=charts,
    )
