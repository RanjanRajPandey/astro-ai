from typing import Dict, List, Optional, Tuple
from models.aspects import (
    AspectCalculationRequest,
    AspectCalculationResponse,
    HouseAspectEntry,
    MutualAspectSummary,
    PlanetToPlanetAspect,
)
from planets.calculator import calculate_planetary_positions
from rules.vedic_constants import ZODIAC_SIGNS
from timezone.resolver import resolve_birth_timestamp


def get_aspect_nature(planet_name: str, sun_lon: float, moon_lon: float) -> str:
    """
    Determines whether a planet's aspect is naturally BENEFIC or MALEFIC.
    - Jupiter, Venus, Mercury: BENEFIC
    - Moon: BENEFIC when Waxing (Shukla Paksha: 0 <= (moon - sun) mod 360 < 180), MALEFIC when Waning
    - Sun, Mars, Saturn, Rahu, Ketu: MALEFIC
    """
    u_name = planet_name.upper()
    if u_name in ("JUPITER", "VENUS", "MERCURY"):
        return "BENEFIC"
    if u_name == "MOON":
        elongation = (moon_lon - sun_lon) % 360.0
        return "BENEFIC" if elongation < 180.0 else "MALEFIC"
    return "MALEFIC"


def get_parashari_house_aspect(
    source_planet: str,
    house_offset: int,
    rahu_ketu_trinal_aspects: bool = True,
    include_pada_drishti: bool = True,
) -> Optional[Tuple[str, bool, bool, str, float, str]]:
    """
    Evaluates the Parashari Graha Drishti of `source_planet` on a house at `house_offset` (1..12).
    Returns (aspect_type, is_full_aspect, is_special_aspect, pada_fraction, virupa_strength, rule_applied)
    or None if no aspect applies at that house offset.
    """
    u_planet = source_planet.upper()

    # 1. Universal 7th House Full Aspect (All 9 Grahas)
    if house_offset == 7:
        return (
            "7TH_FULL",
            True,
            False,
            "4/4",
            60.0,
            "BPHS Ch.26: Universal 7th House Full Drishti (60 Virupas / 100%)",
        )

    # 2. Mars Special Aspects (4th & 8th Houses)
    if u_planet == "MARS" and house_offset in (4, 8):
        return (
            f"{house_offset}TH_SPECIAL_MARS",
            True,
            True,
            "4/4",
            60.0,
            f"BPHS Ch.26: Mars Vishesha (Special) {house_offset}th House Full Drishti (60 Virupas)",
        )

    # 3. Jupiter Special Aspects (5th & 9th Houses)
    if u_planet == "JUPITER" and house_offset in (5, 9):
        return (
            f"{house_offset}TH_SPECIAL_JUPITER",
            True,
            True,
            "4/4",
            60.0,
            f"BPHS Ch.26: Jupiter Vishesha (Special) {house_offset}th House Full Drishti (60 Virupas)",
        )

    # 4. Saturn Special Aspects (3rd & 10th Houses)
    if u_planet == "SATURN" and house_offset in (3, 10):
        prefix = "3RD" if house_offset == 3 else "10TH"
        return (
            f"{prefix}_SPECIAL_SATURN",
            True,
            True,
            "4/4",
            60.0,
            f"BPHS Ch.26: Saturn Vishesha (Special) {house_offset}th House Full Drishti (60 Virupas)",
        )

    # 5. Rahu & Ketu Configurable Trinal Aspects (5th & 9th Houses)
    if (
        u_planet in ("RAHU", "KETU")
        and rahu_ketu_trinal_aspects
        and house_offset in (5, 9)
    ):
        return (
            f"{house_offset}TH_TRINAL_NODE",
            True,
            True,
            "4/4",
            60.0,
            f"Parashari/Nadi Nodal Rule: {source_planet} Trinal {house_offset}th House Full Drishti (60 Virupas)",
        )

    # 6. Partial Parashari Pada Drishti (3/4, 1/2, 1/4) when enabled
    if not include_pada_drishti:
        return None

    if house_offset in (4, 8):
        return (
            f"PADA_3_4_{house_offset}TH",
            False,
            False,
            "3/4",
            45.0,
            f"BPHS Ch.26: Tri-Pada (3/4) {house_offset}th House Partial Drishti (45 Virupas)",
        )
    if house_offset in (5, 9):
        return (
            f"PADA_1_2_{house_offset}TH",
            False,
            False,
            "1/2",
            30.0,
            f"BPHS Ch.26: Dwi-Pada (1/2) {house_offset}th House Partial Drishti (30 Virupas)",
        )
    if house_offset in (3, 10):
        return (
            f"PADA_1_4_{house_offset}TH",
            False,
            False,
            "1/4",
            15.0,
            f"BPHS Ch.26: Eka-Pada (1/4) {house_offset}th House Partial Drishti (15 Virupas)",
        )

    return None


def compute_sphuta_drishti_virupas(
    source_planet: str,
    source_lon: float,
    target_lon: float,
    rahu_ketu_trinal_aspects: bool = True,
) -> float:
    """
    Calculates continuous degree-based BPHS Sphuta Drishti in Virupas (0.0 to 60.0).
    Uses forward zodiacal separation delta = (target_lon - source_lon) % 360.
    """
    u_planet = source_planet.upper()
    delta = (target_lon - source_lon) % 360.0
    if delta < 30.0 or delta > 300.0:
        return 0.0

    # Base BPHS piecewise linear Drishti curve
    if 30.0 <= delta <= 60.0:
        base = (delta - 30.0) / 2.0
    elif 60.0 < delta <= 90.0:
        base = (delta - 60.0) + 15.0
    elif 90.0 < delta <= 120.0:
        base = 45.0 - (delta - 90.0) / 2.0
    elif 120.0 < delta <= 150.0:
        base = 30.0 - (delta - 120.0)
    elif 150.0 < delta <= 180.0:
        base = (delta - 150.0) * 2.0
    else:  # 180.0 < delta <= 300.0
        base = (300.0 - delta) / 2.0

    # Apply Vishesha Drishti (Special Aspect) bonuses in their respective angular zones
    bonus = 0.0
    if u_planet == "MARS":
        # 4th aspect zone (90..120) and 8th aspect zone (210..240)
        if 90.0 <= delta <= 120.0 or 210.0 <= delta <= 240.0:
            bonus = 15.0
    elif u_planet == "JUPITER" or (
        u_planet in ("RAHU", "KETU") and rahu_ketu_trinal_aspects
    ):
        # 5th aspect zone (120..150) and 9th aspect zone (240..270)
        if 120.0 <= delta <= 150.0 or 240.0 <= delta <= 270.0:
            bonus = 30.0
    elif u_planet == "SATURN":
        # 3rd aspect zone (60..90) and 10th aspect zone (270..300)
        if 60.0 <= delta <= 90.0 or 270.0 <= delta <= 300.0:
            bonus = 45.0

    return round(min(60.0, max(0.0, base + bonus)), 3)


def get_ideal_angle_for_offset(house_offset: int) -> float:
    """Returns the ideal cuspal angle (in degrees) for a 1-indexed house offset."""
    return float((house_offset - 1) * 30)


def calculate_all_aspects(request: AspectCalculationRequest) -> AspectCalculationResponse:
    """
    Computes:
    1. All Planet-to-House aspects (Graha-to-Bhava Drishti)
    2. All Planet-to-Planet aspects (Graha-to-Graha Drishti + Sphuta Drishti Virupas)
    3. All Mutual Relationships (Paraspara Drishti & Graha Yuti Conjunctions)
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

    asc_zero_idx = int(planet_res.ascendant_sign_index) - 1  # 0..11

    sun_lon = 0.0
    moon_lon = 0.0
    planet_info: List[Dict] = []

    for p in planet_res.planets:
        u_name = p.planet.upper()
        if u_name == "SUN":
            sun_lon = float(p.longitude)
        elif u_name == "MOON":
            moon_lon = float(p.longitude)

        planet_info.append(
            {
                "planet": p.planet,
                "sign": p.sign,
                "sign_index": p.sign_index,
                "house": p.house,
                "longitude": float(p.longitude),
            }
        )

    house_aspects: List[HouseAspectEntry] = []
    planet_aspects: List[PlanetToPlanetAspect] = []

    # 1. Compute Planet-to-House aspects and Planet-to-Planet aspects
    for src in planet_info:
        src_planet = src["planet"]
        src_house = src["house"]
        src_sign = src["sign"]
        src_lon = src["longitude"]
        nature = get_aspect_nature(src_planet, sun_lon, moon_lon)

        # Evaluate all 12 house offsets (1..12)
        for offset in range(1, 13):
            aspect_eval = get_parashari_house_aspect(
                source_planet=src_planet,
                house_offset=offset,
                rahu_ketu_trinal_aspects=request.rahu_ketu_trinal_aspects,
                include_pada_drishti=request.include_pada_drishti,
            )
            if aspect_eval is None:
                continue

            (
                aspect_type,
                is_full,
                is_special,
                pada_fraction,
                virupa_strength,
                rule_applied,
            ) = aspect_eval

            target_house = ((src_house - 1 + (offset - 1)) % 12) + 1
            target_zero_idx = (asc_zero_idx + (target_house - 1)) % 12
            target_sign = ZODIAC_SIGNS[target_zero_idx]

            house_aspects.append(
                HouseAspectEntry(
                    source_planet=src_planet,
                    source_sign=src_sign,
                    source_house=src_house,
                    target_house=target_house,
                    target_sign=target_sign,
                    house_offset=offset,
                    aspect_type=aspect_type,
                    is_full_aspect=is_full,
                    is_special_aspect=is_special,
                    pada_fraction=pada_fraction,
                    virupa_strength=virupa_strength,
                    aspect_nature=nature,
                    rule_applied=rule_applied,
                )
            )

            # Check if any planets occupy `target_house`
            for tgt in planet_info:
                if tgt["planet"] == src_planet:
                    continue
                if tgt["house"] == target_house:
                    tgt_lon = tgt["longitude"]
                    ang_sep = round((tgt_lon - src_lon) % 360.0, 4)
                    ideal_angle = get_ideal_angle_for_offset(offset)
                    orb_deg = round(abs(ang_sep - ideal_angle), 4)
                    if orb_deg > 180.0:
                        orb_deg = round(360.0 - orb_deg, 4)

                    sphuta_virupas = compute_sphuta_drishti_virupas(
                        source_planet=src_planet,
                        source_lon=src_lon,
                        target_lon=tgt_lon,
                        rahu_ketu_trinal_aspects=request.rahu_ketu_trinal_aspects,
                    )

                    planet_aspects.append(
                        PlanetToPlanetAspect(
                            source_planet=src_planet,
                            source_house=src_house,
                            source_sign=src_sign,
                            source_longitude=round(src_lon, 4),
                            target_planet=tgt["planet"],
                            target_house=target_house,
                            target_sign=tgt["sign"],
                            target_longitude=round(tgt_lon, 4),
                            house_offset=offset,
                            angular_separation_deg=ang_sep,
                            orb_from_exact_aspect_deg=orb_deg,
                            aspect_type=aspect_type,
                            is_full_aspect=is_full,
                            is_special_aspect=is_special,
                            pada_fraction=pada_fraction,
                            virupa_strength=virupa_strength,
                            sphuta_virupa_strength=sphuta_virupas,
                            aspect_nature=nature,
                            rule_applied=rule_applied,
                        )
                    )

    # 2. Compute Mutual Relationships (Conjunctions + Mutual Full Drishti)
    mutual_relationships: List[MutualAspectSummary] = []
    full_aspect_lookup: Dict[Tuple[str, str], PlanetToPlanetAspect] = {
        (pa.source_planet, pa.target_planet): pa
        for pa in planet_aspects
        if pa.is_full_aspect
    }

    n_planets = len(planet_info)
    for i in range(n_planets):
        for j in range(i + 1, n_planets):
            pa = planet_info[i]
            pb = planet_info[j]
            name_a = pa["planet"]
            name_b = pb["planet"]

            # Case A: Same-house Conjunction (Graha Yuti)
            if pa["house"] == pb["house"]:
                diff = abs(pa["longitude"] - pb["longitude"]) % 360.0
                orb = round(min(diff, 360.0 - diff), 4)
                mutual_relationships.append(
                    MutualAspectSummary(
                        planet_a=name_a,
                        house_a=pa["house"],
                        sign_a=pa["sign"],
                        planet_b=name_b,
                        house_b=pb["house"],
                        sign_b=pb["sign"],
                        relationship_type="CONJUNCTION_YUTI",
                        a_to_b_aspect_type="YUTI_1ST",
                        b_to_a_aspect_type="YUTI_1ST",
                        combined_virupa_strength=120.0,
                        exact_orb_deg=orb,
                        description=f"{name_a} and {name_b} are in Graha Yuti (Conjunction) in House {pa['house']} ({pa['sign']}) within {orb:.2f}° orb.",
                    )
                )
                continue

            # Case B: Mutual Full Aspect (Paraspara Drishti)
            ab = full_aspect_lookup.get((name_a, name_b))
            ba = full_aspect_lookup.get((name_b, name_a))

            if ab is not None and ba is not None:
                rel_type = (
                    "MUTUAL_7TH_OPPOSITION"
                    if (ab.house_offset == 7 and ba.house_offset == 7)
                    else "MUTUAL_SPECIAL_LOCK"
                )
                orb = round((ab.orb_from_exact_aspect_deg + ba.orb_from_exact_aspect_deg) / 2.0, 4)
                mutual_relationships.append(
                    MutualAspectSummary(
                        planet_a=name_a,
                        house_a=pa["house"],
                        sign_a=pa["sign"],
                        planet_b=name_b,
                        house_b=pb["house"],
                        sign_b=pb["sign"],
                        relationship_type=rel_type,
                        a_to_b_aspect_type=ab.aspect_type,
                        b_to_a_aspect_type=ba.aspect_type,
                        combined_virupa_strength=round(ab.virupa_strength + ba.virupa_strength, 2),
                        exact_orb_deg=orb,
                        description=f"Paraspara Drishti (Mutual Full Aspect): {name_a} (H{pa['house']}) aspects {name_b} via {ab.aspect_type} and {name_b} (H{pb['house']}) aspects {name_a} via {ba.aspect_type}.",
                    )
                )

    return AspectCalculationResponse(
        utc_datetime_iso=resolved_time.utc_datetime_iso,
        julian_day_ut=planet_res.julian_day_ut,
        ascendant_sign=planet_res.ascendant_sign,
        ayanamsha_type=planet_res.ayanamsha_type,
        rahu_ketu_trinal_aspects=request.rahu_ketu_trinal_aspects,
        house_aspects=house_aspects,
        planet_aspects=planet_aspects,
        mutual_relationships=mutual_relationships,
    )
