from typing import Dict, List, Tuple

from aspects.drishti import calculate_all_aspects, get_aspect_nature
from models.aspects import AspectCalculationRequest
from models.strength import (
    BhavaBalaCalculationRequest,
    BhavaBalaCalculationResponse,
    HouseStrengthEntry,
    PurusharthaSummary,
    ShadbalaCalculationRequest,
)
from planets.calculator import calculate_planetary_positions
from rules.vedic_constants import SANSKRIT_SIGN_NAMES, SIGN_LORDS, ZODIAC_SIGNS
from strength.shadbala import calculate_shadbala_and_vimshopaka
from timezone.resolver import resolve_birth_timestamp

HOUSE_DOMAINS: Dict[int, str] = {
    1: "Tanu Bhava (Self, Vitality, Constitution)",
    2: "Dhana Bhava (Wealth, Speech, Family)",
    3: "Sahaja Bhava (Courage, Siblings, Effort)",
    4: "Sukha Bhava (Home, Mother, Inner Peace)",
    5: "Putra Bhava (Intelligence, Progeny, Purva Punya)",
    6: "Ripu Bhava (Service, Health, Obstacles)",
    7: "Yuvati Bhava (Spouse, Partnerships, Trade)",
    8: "Randhra Bhava (Longevity, Transformation, Occult)",
    9: "Dharma Bhava (Fortune, Guru, Higher Wisdom)",
    10: "Karma Bhava (Career, Authority, Public Action)",
    11: "Labha Bhava (Gains, Aspirations, Elder Network)",
    12: "Vyaya Bhava (Liberation, Foreign Lands,Moksha)",
}

HOUSE_PURUSHARTHAS: Dict[int, str] = {
    1: "DHARMA",
    2: "ARTHA",
    3: "KAMA",
    4: "MOKSHA",
    5: "DHARMA",
    6: "ARTHA",
    7: "KAMA",
    8: "MOKSHA",
    9: "DHARMA",
    10: "ARTHA",
    11: "KAMA",
    12: "MOKSHA",
}


def get_sign_nature_and_zero_house(sign: str) -> Tuple[str, int]:
    """
    Returns (sign_nature, zero_power_house) according to BPHS Ch. 27 Bhava Dig Bala:
    - Nara (Bipedal/Human): Gemini, Virgo, Libra, Aquarius -> Peak in H1, Zero in H7
    - Jalachara (Aquatic): Cancer, Pisces -> Peak in H4, Zero in H10
    - Keeta (Insect): Scorpio -> Peak in H7, Zero in H1
    - Chatushpada (Quadruped): Aries, Taurus, Leo, Sagittarius, Capricorn -> Peak in H10, Zero in H4
    """
    if sign in ("Gemini", "Virgo", "Libra", "Aquarius"):
        return ("NARA_BIPED", 7)
    if sign in ("Cancer", "Pisces"):
        return ("JALACHARA_WATER", 10)
    if sign == "Scorpio":
        return ("KEETA_INSECT", 1)
    return ("CHATUSHPADA_QUADRUPED", 4)


def compute_bhava_dig_bala(house_number: int, sign: str) -> Tuple[str, float]:
    """
    Computes Bhava Dig Bala in Virupas (0.0 to 60.0) for `house_number` (1..12) occupied by `sign`.
    """
    sign_nature, zero_house = get_sign_nature_and_zero_house(sign)
    dist = min((house_number - zero_house) % 12, (zero_house - house_number) % 12)
    return (sign_nature, round(dist * 10.0, 3))


def calculate_bhava_bala(
    request: BhavaBalaCalculationRequest,
) -> BhavaBalaCalculationResponse:
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

    shadbala_res = calculate_shadbala_and_vimshopaka(
        ShadbalaCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )

    aspect_res = calculate_all_aspects(
        AspectCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
            rahu_ketu_trinal_aspects=True,
            include_pada_drishti=True,
        )
    )

    # Map planet -> total_shadbala_virupas
    planet_shadbala_virupas: Dict[str, float] = {
        p.planet.upper(): p.total_shadbala_virupas for p in shadbala_res.planets
    }

    sun_lon = 0.0
    moon_lon = 0.0
    for p in planet_res.planets:
        if p.planet.upper() == "SUN":
            sun_lon = float(p.longitude)
        elif p.planet.upper() == "MOON":
            moon_lon = float(p.longitude)

    # Group planets by occupied house (1..12)
    occupants_by_house: Dict[int, List] = {h: [] for h in range(1, 13)}
    for p in planet_res.planets:
        occupants_by_house[int(p.house)].append(p)

    # Group house aspects by target_house (1..12)
    aspects_by_house: Dict[int, List] = {h: [] for h in range(1, 13)}
    for ha in aspect_res.house_aspects:
        aspects_by_house[int(ha.target_house)].append(ha)

    asc_zero_idx = int(planet_res.ascendant_sign_index) - 1
    raw_houses: List[HouseStrengthEntry] = []

    for h_num in range(1, 13):
        sign_zero_idx = (asc_zero_idx + (h_num - 1)) % 12
        sign = ZODIAC_SIGNS[sign_zero_idx]
        sanskrit_sign = SANSKRIT_SIGN_NAMES[sign]
        lord_planet = SIGN_LORDS[sign]

        # 1. Bhavadhipati Bala (House Lord's Shadbala in Virupas)
        bhavadhipati_bala = round(
            planet_shadbala_virupas.get(lord_planet.upper(), 300.0), 3
        )

        # 2. Bhava Dig Bala (0 to 60 Virupas)
        sign_nature, bhava_dig_bala = compute_bhava_dig_bala(h_num, sign)

        # 3. Bhava Drishti Bala
        drishti_virupas = 0.0
        for ha in aspects_by_house[h_num]:
            src_u = ha.source_planet.upper()
            # A house's own lord aspecting its own house is always supportive
            if src_u == lord_planet.upper():
                drishti_virupas += ha.virupa_strength * 0.5
            elif src_u in ("JUPITER", "MERCURY"):
                # BPHS: Jupiter & Mercury confer full benefic Drishti bonus on Bhavas
                drishti_virupas += ha.virupa_strength * 0.5
            elif ha.aspect_nature == "BENEFIC":
                drishti_virupas += ha.virupa_strength * 0.25
            else:
                drishti_virupas -= ha.virupa_strength * 0.25
        bhava_drishti_bala = round(drishti_virupas, 3)

        # 4. Occupant Factor (Bhava Stithi)
        occ_factor = 0.0
        occ_names: List[str] = []
        for occ in occupants_by_house[h_num]:
            occ_names.append(occ.planet)
            occ_nature = get_aspect_nature(occ.planet, sun_lon, moon_lon)
            if occ.planet.upper() == lord_planet.upper() or occ.dignity in (
                "EXALTED",
                "MOOLATRIKONA",
                "OWN_SIGN",
            ):
                occ_factor += 30.0
            if occ_nature == "BENEFIC":
                occ_factor += 25.0
            else:
                occ_factor -= 10.0 if occ.dignity != "DEBILITATED" else 25.0
        occupant_factor = round(occ_factor, 3)

        total_virupas = round(
            max(
                60.0,
                bhavadhipati_bala
                + bhava_dig_bala
                + bhava_drishti_bala
                + occupant_factor,
            ),
            3,
        )
        total_rupas = round(total_virupas / 60.0, 3)

        if total_rupas >= 8.5:
            grade = "VERY_STRONG"
        elif total_rupas >= 7.0:
            grade = "STRONG"
        elif total_rupas >= 5.5:
            grade = "MODERATE"
        else:
            grade = "WEAK"

        raw_houses.append(
            HouseStrengthEntry(
                house_number=h_num,
                sign=sign,
                sanskrit_sign=sanskrit_sign,
                lord_planet=lord_planet,
                sign_nature=sign_nature,
                purushartha=HOUSE_PURUSHARTHAS[h_num],
                domain_title=HOUSE_DOMAINS[h_num],
                occupants=occ_names,
                bhavadhipati_bala=bhavadhipati_bala,
                bhava_dig_bala=bhava_dig_bala,
                bhava_drishti_bala=bhava_drishti_bala,
                occupant_factor=occupant_factor,
                total_bhava_bala_virupas=total_virupas,
                total_bhava_bala_rupas=total_rupas,
                strength_grade=grade,
                rank=1,
            )
        )

    sorted_houses = sorted(
        raw_houses, key=lambda x: x.total_bhava_bala_virupas, reverse=True
    )
    rank_map = {h.house_number: idx + 1 for idx, h in enumerate(sorted_houses)}

    final_houses = [
        h.model_copy(update={"rank": rank_map[h.house_number]}) for h in raw_houses
    ]

    avg_rupas = round(
        sum(h.total_bhava_bala_rupas for h in final_houses) / 12.0, 3
    )

    purushartha_groups = [
        ("DHARMA", [1, 5, 9]),
        ("ARTHA", [2, 6, 10]),
        ("KAMA", [3, 7, 11]),
        ("MOKSHA", [4, 8, 12]),
    ]
    purushartha_summaries: List[PurusharthaSummary] = []
    house_by_num = {h.house_number: h for h in final_houses}

    for p_name, p_houses in purushartha_groups:
        group_entries = [house_by_num[hn] for hn in p_houses]
        g_avg = round(
            sum(e.total_bhava_bala_rupas for e in group_entries) / 3.0, 3
        )
        dom = max(group_entries, key=lambda e: e.total_bhava_bala_rupas).house_number
        purushartha_summaries.append(
            PurusharthaSummary(
                purushartha=p_name,
                houses=p_houses,
                average_rupas=g_avg,
                dominant_house=dom,
            )
        )

    return BhavaBalaCalculationResponse(
        utc_datetime_iso=resolved_time.utc_datetime_iso,
        julian_day_ut=planet_res.julian_day_ut,
        ascendant_sign=planet_res.ascendant_sign,
        ayanamsha_type=planet_res.ayanamsha_type,
        strongest_house=sorted_houses[0].house_number,
        weakest_house=sorted_houses[-1].house_number,
        average_rupas=avg_rupas,
        purushartha_summaries=purushartha_summaries,
        houses=final_houses,
    )
