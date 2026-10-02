from typing import Dict, List, Set, Tuple

from aspects.drishti import calculate_all_aspects
from models.aspects import AspectCalculationRequest
from models.yogas import (
    YogaCalculationRequest,
    YogaCalculationResponse,
    YogaEvaluationEntry,
)
from planets.calculator import calculate_planetary_positions
from rules.vedic_constants import EXALTATION_RULES, SIGN_LORDS, ZODIAC_SIGNS
from timezone.resolver import resolve_birth_timestamp


def _offset_between_houses(from_house: int, to_house: int) -> int:
    """Returns 1-indexed inclusive house offset (1..12) from `from_house` to `to_house`."""
    return ((to_house - from_house) % 12) + 1


def _are_connected(
    planet_a: str,
    planet_b: str,
    p_map: Dict[str, dict],
    mutual_pairs: Set[Tuple[str, str]],
) -> Tuple[bool, str]:
    """
    Checks if two planets have classical Sambandha:
    1. Conjunction (Yuti in same house)
    2. Mutual Full Aspect (Paraspara Drishti)
    3. Sign Exchange (Parivartana)
    """
    if planet_a == planet_b:
        return (True, f"{planet_a} is dual lord of both houses")
    pa = p_map[planet_a]
    pb = p_map[planet_b]

    if pa["house"] == pb["house"]:
        return (
            True,
            f"{planet_a} and {planet_b} are in Graha Yuti (Conjunction) in House {pa['house']} ({pa['sign']})",
        )
    if (planet_a, planet_b) in mutual_pairs or (planet_b, planet_a) in mutual_pairs:
        return (
            True,
            f"{planet_a} (H{pa['house']}) and {planet_b} (H{pb['house']}) are in Paraspara Drishti (Mutual Full Aspect)",
        )
    if SIGN_LORDS[pa["sign"]] == planet_b and SIGN_LORDS[pb["sign"]] == planet_a:
        return (
            True,
            f"{planet_a} (in {pa['sign']}) and {planet_b} (in {pb['sign']}) are in Parivartana (Sign Exchange)",
        )
    return (False, f"No direct Yuti, Paraspara Drishti, or Parivartana between {planet_a} (H{pa['house']}) and {planet_b} (H{pb['house']})")


def detect_all_yogas(request: YogaCalculationRequest) -> YogaCalculationResponse:
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
            include_pada_drishti=False,
        )
    )

    asc_zero_idx = int(planet_res.ascendant_sign_index) - 1
    house_sign: Dict[int, str] = {}
    house_lord: Dict[int, str] = {}
    for h in range(1, 13):
        s_name = ZODIAC_SIGNS[(asc_zero_idx + h - 1) % 12]
        house_sign[h] = s_name
        house_lord[h] = SIGN_LORDS[s_name]

    p_map: Dict[str, dict] = {}
    for p in planet_res.planets:
        p_map[p.planet] = {
            "planet": p.planet,
            "sign": p.sign,
            "sign_index": int(p.sign_index),
            "house": int(p.house),
            "longitude": float(p.longitude),
            "dignity": p.dignity,
            "is_retrograde": p.is_retrograde,
            "is_combust": p.is_combust,
        }

    mutual_pairs: Set[Tuple[str, str]] = set()
    for m in aspect_res.mutual_relationships:
        if m.relationship_type in ("MUTUAL_7TH_OPPOSITION", "MUTUAL_SPECIAL_LOCK"):
            mutual_pairs.add((m.planet_a, m.planet_b))
            mutual_pairs.add((m.planet_b, m.planet_a))

    full_aspect_pairs: Set[Tuple[str, str]] = {
        (pa.source_planet, pa.target_planet)
        for pa in aspect_res.planet_aspects
        if pa.is_full_aspect
    }

    evaluated: List[YogaEvaluationEntry] = []

    # =========================================================================
    # 1. PANCHA MAHAPURUSHA YOGAS (Mars, Mercury, Jupiter, Venus, Saturn)
    # =========================================================================
    mahapurusha_specs = [
        (
            "RUCHAKA_MAHAPURUSHA",
            "Ruchaka Mahapurusha Yoga",
            "रुचक महापुरुष योग",
            "Mars",
            "Mars placed in a Kendra (1, 4, 7, 10) from Lagna in its Exaltation (Capricorn) or Own Sign (Aries, Scorpio).",
            "Confers valor, physical vitality, decisive leadership, command over land/engineering, and victory over obstacles.",
        ),
        (
            "BHADRA_MAHAPURUSHA",
            "Bhadra Mahapurusha Yoga",
            "भद्र महापुरुष योग",
            "Mercury",
            "Mercury placed in a Kendra (1, 4, 7, 10) from Lagna in its Exaltation/Own Sign (Virgo, Gemini).",
            "Bestows sharp intellect, eloquence, analytical mastery, commercial acumen, and scholarly renown.",
        ),
        (
            "HAMSA_MAHAPURUSHA",
            "Hamsa Mahapurusha Yoga",
            "हंस महापुरुष योग",
            "Jupiter",
            "Jupiter placed in a Kendra (1, 4, 7, 10) from Lagna in its Exaltation (Cancer) or Own Sign (Sagittarius, Pisces).",
            "Grants spiritual wisdom, ethical authority, wealth, respected mentorship, and dharmic grace.",
        ),
        (
            "MALAVYA_MAHAPURUSHA",
            "Malavya Mahapurusha Yoga",
            "मालव्य महापुरुष योग",
            "Venus",
            "Venus placed in a Kendra (1, 4, 7, 10) from Lagna in its Exaltation (Pisces) or Own Sign (Taurus, Libra).",
            "Bestows artistic refinement, prosperity, harmonious partnerships, vehicles, and aesthetic magnetism.",
        ),
        (
            "SHASHA_MAHAPURUSHA",
            "Shasha Mahapurusha Yoga",
            "शश महापुरुष योग",
            "Saturn",
            "Saturn placed in a Kendra (1, 4, 7, 10) from Lagna in its Exaltation (Libra) or Own Sign (Capricorn, Aquarius).",
            "Confers organizational authority, public governance, endurance, discipline, and lasting structural achievements.",
        ),
    ]

    for code, name, sanskrit, p_name, defn, effect in mahapurusha_specs:
        pd = p_map[p_name]
        in_kendra = pd["house"] in (1, 4, 7, 10)
        high_dig = pd["dignity"] in ("EXALTED", "MOOLATRIKONA", "OWN_SIGN")
        formed = in_kendra and high_dig
        strength = (
            "VERY_STRONG"
            if (formed and pd["dignity"] == "EXALTED")
            else ("STRONG" if formed else "INACTIVE")
        )
        evaluated.append(
            YogaEvaluationEntry(
                yoga_code=code,
                name=name,
                sanskrit_name=sanskrit,
                category="PANCHA_MAHAPURUSHA",
                definition=defn,
                classical_Effect=effect,
                required_conditions=[
                    f"{p_name} must occupy a Kendra house (1, 4, 7, or 10) from Ascendant",
                    f"{p_name} must be in Exalted, Moolatrikona, or Own Sign dignity",
                ],
                detected_conditions=[
                    f"{p_name} is in House {pd['house']} ({'Kendra ✓' if in_kendra else 'Not Kendra ✗'})",
                    f"{p_name} is in {pd['sign']} with dignity {pd['dignity']} ({'Qualifies ✓' if high_dig else 'Does not qualify ✗'})",
                ],
                planets_involved=[p_name],
                houses_involved=[pd["house"]],
                status="ACTIVE" if formed else "NOT_FORMED",
                strength=strength,
                is_benefic=True,
            )
        )

    # =========================================================================
    # 2. LUNAR YOGAS (Gajakesari, Sunapha, Anapha, Durudhura, Kemadruma, Chandra-Mangala, Adhi, Shakata)
    # =========================================================================
    moon = p_map["Moon"]
    jup = p_map["Jupiter"]
    jup_offset_from_moon = _offset_between_houses(moon["house"], jup["house"])
    gajakesari_formed = jup_offset_from_moon in (1, 4, 7, 10)
    gk_strength = (
        "VERY_STRONG"
        if (gajakesari_formed and jup["dignity"] in ("EXALTED", "MOOLATRIKONA", "OWN_SIGN", "GREAT_FRIEND"))
        else ("STRONG" if gajakesari_formed else "INACTIVE")
    )
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="GAJAKESARI_YOGA",
            name="Gajakesari Yoga",
            sanskrit_name="गजकेसरी योग",
            category="LUNAR_YOGA",
            definition="Jupiter placed in a Kendra (1st, 4th, 7th, or 10th house) counted from the natal Moon.",
            classical_Effect="Bestows lasting reputation, noble character, emotional resilience, intelligence, and protection in adversity.",
            required_conditions=[
                "Jupiter must be in 1st, 4th, 7th, or 10th house from Moon",
            ],
            detected_conditions=[
                f"Moon is in House {moon['house']} ({moon['sign']}) and Jupiter is in House {jup['house']} ({jup['sign']})",
                f"Jupiter is in the {jup_offset_from_moon}th house from Moon ({'Kendra from Moon ✓' if gajakesari_formed else 'Not Kendra from Moon ✗'})",
            ],
            planets_involved=["Moon", "Jupiter"],
            houses_involved=sorted(list({moon["house"], jup["house"]})),
            status="ACTIVE" if gajakesari_formed else "NOT_FORMED",
            strength=gk_strength,
            is_benefic=True,
        )
    )

    # Sunapha (2nd from Moon), Anapha (12th from Moon), Durudhura (both), Kemadruma (neither)
    starry_planets = ["Mars", "Mercury", "Jupiter", "Venus", "Saturn"]
    second_from_moon = [
        p for p in starry_planets if _offset_between_houses(moon["house"], p_map[p]["house"]) == 2
    ]
    twelfth_from_moon = [
        p for p in starry_planets if _offset_between_houses(moon["house"], p_map[p]["house"]) == 12
    ]

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="SUNAPHA_YOGA",
            name="Sunapha Yoga",
            sanskrit_name="सुनफा योग",
            category="LUNAR_YOGA",
            definition="Any starry planet (Mars, Mercury, Jupiter, Venus, Saturn) occupying the 2nd house from the Moon.",
            classical_Effect="Confers self-earned wealth, intelligence, financial resourcefulness, and good speech.",
            required_conditions=["At least one planet (excluding Sun, Rahu, Ketu) in 2nd house from Moon"],
            detected_conditions=[
                f"2nd house from Moon (H{((moon['house']) % 12) + 1}) occupied by: {', '.join(second_from_moon) if second_from_moon else 'None'}"
            ],
            planets_involved=["Moon"] + second_from_moon,
            houses_involved=[moon["house"], ((moon["house"]) % 12) + 1],
            status="ACTIVE" if len(second_from_moon) > 0 else "NOT_FORMED",
            strength="STRONG" if len(second_from_moon) > 0 else "INACTIVE",
            is_benefic=True,
        )
    )

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="ANAPHA_YOGA",
            name="Anapha Yoga",
            sanskrit_name="अनफा योग",
            category="LUNAR_YOGA",
            definition="Any starry planet (Mars, Mercury, Jupiter, Venus, Saturn) occupying the 12th house from the Moon.",
            classical_Effect="Bestows magnetic personality, dignified bearing, spiritual inclination, and comfort.",
            required_conditions=["At least one planet (excluding Sun, Rahu, Ketu) in 12th house from Moon"],
            detected_conditions=[
                f"12th house from Moon (H{((moon['house'] - 2) % 12) + 1}) occupied by: {', '.join(twelfth_from_moon) if twelfth_from_moon else 'None'}"
            ],
            planets_involved=["Moon"] + twelfth_from_moon,
            houses_involved=[moon["house"], ((moon["house"] - 2) % 12) + 1],
            status="ACTIVE" if len(twelfth_from_moon) > 0 else "NOT_FORMED",
            strength="STRONG" if len(twelfth_from_moon) > 0 else "INACTIVE",
            is_benefic=True,
        )
    )

    durudhura_formed = len(second_from_moon) > 0 and len(twelfth_from_moon) > 0
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="DURUDHURA_YOGA",
            name="Durudhura Yoga",
            sanskrit_name="दुरुधरा योग",
            category="LUNAR_YOGA",
            definition="Starry planets flanking the Moon in both the 2nd and 12th houses from the Moon.",
            classical_Effect="Grants abundant wealth, vehicles, generous nature, and strong emotional support.",
            required_conditions=[
                "At least one starry planet in 2nd from Moon AND at least one starry planet in 12th from Moon"
            ],
            detected_conditions=[
                f"2nd from Moon: {', '.join(second_from_moon) if second_from_moon else 'None'}; 12th from Moon: {', '.join(twelfth_from_moon) if twelfth_from_moon else 'None'}"
            ],
            planets_involved=["Moon"] + second_from_moon + twelfth_from_moon,
            houses_involved=[moon["house"], ((moon["house"]) % 12) + 1, ((moon["house"] - 2) % 12) + 1],
            status="ACTIVE" if durudhura_formed else "NOT_FORMED",
            strength="VERY_STRONG" if durudhura_formed else "INACTIVE",
            is_benefic=True,
        )
    )

    # Kemadruma Dosha & Kemadruma Bhanga (Cancellation)
    no_flanking = len(second_from_moon) == 0 and len(twelfth_from_moon) == 0
    kendra_from_moon = [
        p for p in starry_planets if _offset_between_houses(moon["house"], p_map[p]["house"]) in (1, 4, 7, 10)
    ]
    kendra_from_lagna = [
        p for p in starry_planets if p_map[p]["house"] in (1, 4, 7, 10)
    ]
    if no_flanking:
        if len(kendra_from_moon) > 0 or len(kendra_from_lagna) > 0:
            kem_status = "CANCELLED_OR_MITIGATED"
            kem_strength = "MITIGATED"
            kem_obs = [
                "No starry planets in 2nd or 12th from Moon (initial Kemadruma trigger)",
                f"Kemadruma Bhanga (Cancellation) active: Kendra from Moon occupied by [{', '.join(kendra_from_moon)}] and Kendra from Lagna occupied by [{', '.join(kendra_from_lagna)}]",
            ]
        else:
            kem_status = "ACTIVE"
            kem_strength = "MODERATE"
            kem_obs = ["No starry planets in 2nd, 12th, or Kendra from Moon/Lagna"]
    else:
        kem_status = "NOT_FORMED"
        kem_strength = "INACTIVE"
        kem_obs = ["Moon is flanked by planets in 2nd/12th; Kemadruma does not arise"]

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="KEMADRUMA_DOSHA",
            name="Kemadruma Dosha (with Bhanga Check)",
            sanskrit_name="केमद्रुम दोष / भंग",
            category="DOSHA",
            definition="Absence of starry planets in 2nd and 12th from Moon; cancelled (Kalpadruma/Kemadruma Bhanga) if planets occupy Kendra from Lagna or Moon.",
            classical_Effect="When uncancelled, indicates solitary struggles; when cancelled (Kemadruma Bhanga), transforms struggle into self-made resilience.",
            required_conditions=["No starry planets in 2nd or 12th from Moon", "Checked against Kendra cancellation rules"],
            detected_conditions=kem_obs,
            planets_involved=["Moon"],
            houses_involved=[moon["house"]],
            status=kem_status,
            strength=kem_strength,
            is_benefic=False,
        )
    )

    # Chandra-Mangala Yoga
    mars = p_map["Mars"]
    cm_conj = moon["house"] == mars["house"]
    cm_mutual = ("Moon", "Mars") in mutual_pairs
    cm_formed = cm_conj or cm_mutual
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="CHANDRA_MANGALA_YOGA",
            name="Chandra-Mangala Yoga",
            sanskrit_name="चन्द्र-मंगल योग",
            category="DHANA_YOGA",
            definition="Moon and Mars in conjunction (same sign) or mutual 7th aspect.",
            classical_Effect="Generates strong financial drive, entrepreneurial courage, and material resourcefulness.",
            required_conditions=["Moon and Mars must be conjunct or in mutual 7th aspect"],
            detected_conditions=[
                f"Moon is in H{moon['house']} ({moon['sign']}) and Mars is in H{mars['house']} ({mars['sign']}) — {'Connected ✓' if cm_formed else 'Not connected ✗'}"
            ],
            planets_involved=["Moon", "Mars"],
            houses_involved=sorted(list({moon["house"], mars["house"]})),
            status="ACTIVE" if cm_formed else "NOT_FORMED",
            strength="STRONG" if cm_formed else "INACTIVE",
            is_benefic=True,
        )
    )

    # Adhi Yoga (Mercury, Jupiter, Venus in 6, 7, 8 from Moon)
    adhi_benefics = [
        b
        for b in ("Mercury", "Jupiter", "Venus")
        if _offset_between_houses(moon["house"], p_map[b]["house"]) in (6, 7, 8)
    ]
    adhi_formed = len(adhi_benefics) >= 2
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="ADHI_YOGA",
            name="Chandra Adhi Yoga",
            sanskrit_name="चन्द्र अधि योग",
            category="LUNAR_YOGA",
            definition="Natural benefics (Mercury, Jupiter, Venus) occupying the 6th, 7th, and/or 8th houses from the Moon.",
            classical_Effect="Bestows leadership, trustworthiness, victory over competitors, and long-term prosperity.",
            required_conditions=["Two or more natural benefics (Mercury, Jupiter, Venus) in 6th, 7th, or 8th from Moon"],
            detected_conditions=[
                f"Benefics in 6th/7th/8th from Moon: {', '.join(adhi_benefics) if adhi_benefics else 'None'} ({len(adhi_benefics)}/3)"
            ],
            planets_involved=["Moon"] + adhi_benefics,
            houses_involved=sorted(list({moon["house"]} | {p_map[b]["house"] for b in adhi_benefics})),
            status="ACTIVE" if adhi_formed else "NOT_FORMED",
            strength="VERY_STRONG" if len(adhi_benefics) == 3 else ("STRONG" if adhi_formed else "INACTIVE"),
            is_benefic=True,
        )
    )

    # =========================================================================
    # 3. SOLAR YOGAS (Budhaditya, Vesi, Vosi, Ubhayachari)
    # =========================================================================
    sun = p_map["Sun"]
    merc = p_map["Mercury"]
    budhaditya_formed = sun["house"] == merc["house"]
    ba_strength = (
        "VERY_STRONG"
        if (budhaditya_formed and not merc["is_combust"] and sun["house"] in (1, 4, 5, 7, 9, 10, 11))
        else ("STRONG" if budhaditya_formed else "INACTIVE")
    )
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="BUDHADITYA_YOGA",
            name="Budhaditya Nipuna Yoga",
            sanskrit_name="बुधादित्य निपुण योग",
            category="SOLAR_YOGA",
            definition="Sun and Mercury conjoined in the same zodiac sign / house.",
            classical_Effect="Confers high intelligence, administrative skill, scholarly reputation, and analytical clarity.",
            required_conditions=["Sun and Mercury must occupy the same sign/house"],
            detected_conditions=[
                f"Sun is in H{sun['house']} ({sun['sign']}) and Mercury is in H{merc['house']} ({merc['sign']})",
                f"Conjunction status: {'Active ✓' if budhaditya_formed else 'Not conjunct ✗'}"
                + (f" (Mercury combust: {merc['is_combust']})" if budhaditya_formed else ""),
            ],
            planets_involved=["Sun", "Mercury"],
            houses_involved=sorted(list({sun["house"], merc["house"]})),
            status="ACTIVE" if budhaditya_formed else "NOT_FORMED",
            strength=ba_strength,
            is_benefic=True,
        )
    )

    vesi_planets = [
        p for p in starry_planets if _offset_between_houses(sun["house"], p_map[p]["house"]) == 2
    ]
    vosi_planets = [
        p for p in starry_planets if _offset_between_houses(sun["house"], p_map[p]["house"]) == 12
    ]
    ubhayachari_formed = len(vesi_planets) > 0 and len(vosi_planets) > 0

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="VESI_YOGA",
            name="Vesi Yoga",
            sanskrit_name="वेसी योग",
            category="SOLAR_YOGA",
            definition="Starry planets (Mars, Mercury, Jupiter, Venus, Saturn) occupying the 2nd house from the Sun.",
            classical_Effect="Bestows truthfulness, balanced judgment, eloquent speech, and steady status.",
            required_conditions=["At least one starry planet in 2nd house from Sun"],
            detected_conditions=[
                f"2nd from Sun occupied by: {', '.join(vesi_planets) if vesi_planets else 'None'}"
            ],
            planets_involved=["Sun"] + vesi_planets,
            houses_involved=[sun["house"], ((sun["house"]) % 12) + 1],
            status="ACTIVE" if len(vesi_planets) > 0 else "NOT_FORMED",
            strength="STRONG" if len(vesi_planets) > 0 else "INACTIVE",
            is_benefic=True,
        )
    )

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="VOSI_YOGA",
            name="Vosi Yoga",
            sanskrit_name="वोसी योग",
            category="SOLAR_YOGA",
            definition="Starry planets (Mars, Mercury, Jupiter, Venus, Saturn) occupying the 12th house from the Sun.",
            classical_Effect="Confers deep learning, charitable nature, technical mastery, and strong memory.",
            required_conditions=["At least one starry planet in 12th house from Sun"],
            detected_conditions=[
                f"12th from Sun occupied by: {', '.join(vosi_planets) if vosi_planets else 'None'}"
            ],
            planets_involved=["Sun"] + vosi_planets,
            houses_involved=[sun["house"], ((sun["house"] - 2) % 12) + 1],
            status="ACTIVE" if len(vosi_planets) > 0 else "NOT_FORMED",
            strength="STRONG" if len(vosi_planets) > 0 else "INACTIVE",
            is_benefic=True,
        )
    )

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="UBHAYACHARI_YOGA",
            name="Ubhayachari Yoga",
            sanskrit_name="उभयचारी योग",
            category="SOLAR_YOGA",
            definition="Starry planets flanking the Sun in both the 2nd and 12th houses from the Sun.",
            classical_Effect="Creates royal bearing, strong constitution, leadership capacity, and material affluence.",
            required_conditions=["Starry planets in both 2nd and 12th houses from Sun"],
            detected_conditions=[
                f"2nd from Sun: {', '.join(vesi_planets) if vesi_planets else 'None'}; 12th from Sun: {', '.join(vosi_planets) if vosi_planets else 'None'}"
            ],
            planets_involved=["Sun"] + vesi_planets + vosi_planets,
            houses_involved=[sun["house"], ((sun["house"]) % 12) + 1, ((sun["house"] - 2) % 12) + 1],
            status="ACTIVE" if ubhayachari_formed else "NOT_FORMED",
            strength="VERY_STRONG" if ubhayachari_formed else "INACTIVE",
            is_benefic=True,
        )
    )

    # =========================================================================
    # 4. RAJA YOGAS, VIPARITA RAJA YOGAS, DHANA YOGAS & PARIVARTANA
    # =========================================================================
    lord_9 = house_lord[9]
    lord_10 = house_lord[10]
    dk_connected, dk_reason = _are_connected(lord_9, lord_10, p_map, mutual_pairs)
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="DHARMA_KARMADHIPATI_RAJA_YOGA",
            name="Dharma-Karmadhipati Raja Yoga",
            sanskrit_name="धर्म-कर्माधिपति राजयोग",
            category="RAJA_YOGA",
            definition="Connection (conjunction, mutual full aspect, or sign exchange) between the 9th Lord (Dharma/Fortune) and 10th Lord (Karma/Career).",
            classical_Effect="Supreme Raja Yoga for career eminence, ethical leadership, high public office, and enduring fortune.",
            required_conditions=[
                f"9th Lord ({lord_9}) and 10th Lord ({lord_10}) must be in Yuti, Paraspara Drishti, or Parivartana"
            ],
            detected_conditions=[dk_reason],
            planets_involved=list(dict.fromkeys([lord_9, lord_10])),
            houses_involved=sorted(list({9, 10, p_map[lord_9]["house"], p_map[lord_10]["house"]})),
            status="ACTIVE" if dk_connected else "NOT_FORMED",
            strength="VERY_STRONG" if dk_connected else "INACTIVE",
            is_benefic=True,
        )
    )

    # General Kendra-Trikona Raja Yoga (any Kendra lord 1,4,7,10 connected with Trikona lord 1,5,9)
    kendra_lords = {h: house_lord[h] for h in (1, 4, 7, 10)}
    trikona_lords = {h: house_lord[h] for h in (1, 5, 9)}
    kt_connections: List[str] = []
    kt_planets: Set[str] = set()
    kt_houses: Set[int] = set()
    for kh, kl in kendra_lords.items():
        for th, tl in trikona_lords.items():
            if kh == th:
                continue
            if kl == tl:
                # Single planet ruling both a Kendra and a Trikona (Yogakaraka!)
                kt_connections.append(
                    f"Yogakaraka {kl} rules both Kendra H{kh} and Trikona H{th} (placed in H{p_map[kl]['house']})"
                )
                kt_planets.add(kl)
                kt_houses.update([kh, th, p_map[kl]["house"]])
            else:
                conn, reason = _are_connected(kl, tl, p_map, mutual_pairs)
                if conn:
                    kt_connections.append(f"Kendra H{kh} Lord ({kl}) + Trikona H{th} Lord ({tl}): {reason}")
                    kt_planets.update([kl, tl])
                    kt_houses.update([kh, th, p_map[kl]["house"], p_map[tl]["house"]])

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="KENDRA_TRIKONA_RAJA_YOGA",
            name="Parashari Kendra-Trikona Raja Yoga",
            sanskrit_name="केन्द्र-त्रिकोण राजयोग",
            category="RAJA_YOGA",
            definition="Sambandha (conjunction, mutual aspect, exchange, or Yogakaraka dual-lordship) between lords of Kendra (1, 4, 7, 10) and Trikona (1, 5, 9) houses.",
            classical_Effect="Elevates status, career authority, recognition, and life achievement.",
            required_conditions=["Connection between any Kendra Lord (1, 4, 7, 10) and Trikona Lord (1, 5, 9)"],
            detected_conditions=kt_connections if kt_connections else ["No Kendra-Trikona lord pairs are in direct Yuti/Mutual Aspect/Exchange"],
            planets_involved=sorted(list(kt_planets)),
            houses_involved=sorted(list(kt_houses)) if kt_houses else [1, 5, 9, 10],
            status="ACTIVE" if len(kt_connections) > 0 else "NOT_FORMED",
            strength="VERY_STRONG" if len(kt_connections) >= 2 else ("STRONG" if len(kt_connections) == 1 else "INACTIVE"),
            is_benefic=True,
        )
    )

    # Lakshmi Dhana Yoga (Wealth connections among 1, 2, 5, 9, 11 lords)
    dhana_houses = [2, 5, 9, 11]
    dhana_connections: List[str] = []
    dhana_planets: Set[str] = set()
    for i in range(len(dhana_houses)):
        for j in range(i + 1, len(dhana_houses)):
            h_a = dhana_houses[i]
            h_b = dhana_houses[j]
            l_a = house_lord[h_a]
            l_b = house_lord[h_b]
            conn, reason = _are_connected(l_a, l_b, p_map, mutual_pairs)
            if conn:
                dhana_connections.append(f"H{h_a} Lord ({l_a}) & H{h_b} Lord ({l_b}): {reason}")
                dhana_planets.update([l_a, l_b])

    # Also check if 2nd lord is in 11th or 11th lord is in 2nd
    if p_map[house_lord[2]]["house"] == 11:
        dhana_connections.append(f"2nd Lord ({house_lord[2]}) is placed directly in 11th House of Gains")
        dhana_planets.add(house_lord[2])
    if p_map[house_lord[11]]["house"] == 2:
        dhana_connections.append(f"11th Lord ({house_lord[11]}) is placed directly in 2nd House of Wealth")
        dhana_planets.add(house_lord[11])

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="LAKSHMI_DHANA_YOGA",
            name="Mahalakshmi Dhana Yoga",
            sanskrit_name="महालक्ष्मी धन योग",
            category="DHANA_YOGA",
            definition="Sambandha or placement link among the wealth-producing houses: 2nd (Dhana), 5th (Purva Punya), 9th (Bhagya), and 11th (Labha).",
            classical_Effect="Produces financial abundance, multiple income streams, asset accumulation, and material security.",
            required_conditions=["Connection or placement link between lords of 2nd, 5th, 9th, and 11th houses"],
            detected_conditions=dhana_connections if dhana_connections else ["No direct conjunction/mutual aspect between 2nd, 5th, 9th, and 11th lords"],
            planets_involved=sorted(list(dhana_planets)),
            houses_involved=[2, 5, 9, 11],
            status="ACTIVE" if len(dhana_connections) > 0 else "NOT_FORMED",
            strength="VERY_STRONG" if len(dhana_connections) >= 2 else ("STRONG" if len(dhana_connections) == 1 else "INACTIVE"),
            is_benefic=True,
        )
    )

    # Viparita Raja Yogas (Harsha: 6L in 6/8/12, Sarala: 8L in 6/8/12, Vimala: 12L in 6/8/12)
    viparita_specs = [
        ("VIPARITA_HARSHA_RAJA_YOGA", "Harsha Viparita Raja Yoga", "हर्ष विपरीत राजयोग", 6, "Invincibility over competitors, good health, and rise after overcoming obstacles."),
        ("VIPARITA_SARALA_RAJA_YOGA", "Sarala Viparita Raja Yoga", "सरल विपरीत राजयोग", 8, "Fearlessness, longevity, scholarly depth, and sudden breakthroughs through crisis."),
        ("VIPARITA_VIMALA_RAJA_YOGA", "Vimala Viparita Raja Yoga", "विमल विपरीत राजयोग", 12, "Independent ethics, financial contentment, spiritual dignity, and freedom from debt."),
    ]
    for v_code, v_name, v_sanskrit, d_house, v_effect in viparita_specs:
        d_lord = house_lord[d_house]
        placed_h = p_map[d_lord]["house"]
        v_formed = placed_h in (6, 8, 12)
        evaluated.append(
            YogaEvaluationEntry(
                yoga_code=v_code,
                name=v_name,
                sanskrit_name=v_sanskrit,
                category="VIPARITA_RAJA_YOGA",
                definition=f"The {d_house}th Lord (Dusthana ruler) placed in one of the Dusthana houses (6th, 8th, or 12th).",
                classical_Effect=v_effect,
                required_conditions=[f"Lord of House {d_house} ({d_lord}) must occupy House 6, 8, or 12"],
                detected_conditions=[
                    f"House {d_house} Lord ({d_lord}) is placed in House {placed_h} ({'Dusthana placement ✓' if v_formed else 'Not in 6/8/12 ✗'})"
                ],
                planets_involved=[d_lord],
                houses_involved=sorted(list({d_house, placed_h})),
                status="ACTIVE" if v_formed else "NOT_FORMED",
                strength="STRONG" if v_formed else "INACTIVE",
                is_benefic=True,
            )
        )

    # Neecha Bhanga Raja Yoga (Debilitation Cancellation)
    debilitated_planets = [p for p, d in p_map.items() if d["dignity"] == "DEBILITATED"]
    nb_reasons: List[str] = []
    nb_planets: List[str] = []
    for dp in debilitated_planets:
        d_info = p_map[dp]
        sign_dispositor = SIGN_LORDS[d_info["sign"]]
        # Find planet exalted in d_info["sign"]
        exalt_lord = None
        for cand, (ex_sign, _) in EXALTATION_RULES.items():
            if ex_sign == d_info["sign"]:
                exalt_lord = cand
                break

        disp_in_kendra_lagna = p_map[sign_dispositor]["house"] in (1, 4, 7, 10)
        disp_in_kendra_moon = _offset_between_houses(moon["house"], p_map[sign_dispositor]["house"]) in (1, 4, 7, 10)
        disp_aspects = (sign_dispositor, dp) in full_aspect_pairs
        exalt_in_kendra = (
            exalt_lord is not None
            and p_map.get(exalt_lord) is not None
            and p_map[exalt_lord]["house"] in (1, 4, 7, 10)
        )

        if disp_in_kendra_lagna or disp_in_kendra_moon or disp_aspects or exalt_in_kendra:
            nb_planets.append(dp)
            nb_reasons.append(
                f"Debilitated {dp} in {d_info['sign']} (H{d_info['house']}) achieves Neecha Bhanga via dispositor {sign_dispositor} (H{p_map[sign_dispositor]['house']})"
            )

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="NEECHA_BHANGA_RAJA_YOGA",
            name="Neecha Bhanga Raja Yoga",
            sanskrit_name="नीचभंग राजयोग",
            category="RAJA_YOGA",
            definition="Cancellation of a planet's debilitation when its sign dispositor or exaltation lord occupies a Kendra from Lagna/Moon or aspects the planet.",
            classical_Effect="Transforms initial vulnerability or humble beginnings into extraordinary mastery and resilience.",
            required_conditions=[
                "A planet is in its debilitation sign AND its dispositor/exaltation lord is in Kendra from Lagna/Moon or aspects it"
            ],
            detected_conditions=nb_reasons
            if nb_reasons
            else (
                [f"Debilitated planet(s) {', '.join(debilitated_planets)} without Kendra cancellation"]
                if debilitated_planets
                else ["No planets are debilitated in D1 Rashi chart"]
            ),
            planets_involved=nb_planets if nb_planets else debilitated_planets,
            houses_involved=[p_map[p]["house"] for p in (nb_planets or debilitated_planets)] or [1],
            status="ACTIVE" if len(nb_reasons) > 0 else "NOT_FORMED",
            strength="VERY_STRONG" if len(nb_reasons) > 0 else "INACTIVE",
            is_benefic=True,
        )
    )

    # =========================================================================
    # 5. CLASSICAL DOSHAS (Mangal/Kuja Dosha, Kala Sarpa, Guru Chandala)
    # =========================================================================
    # Mangal / Kuja Dosha (Mars in 1, 2, 4, 7, 8, 12 from Lagna + Parihara check)
    mars_h = mars["house"]
    kuja_raw = mars_h in (1, 2, 4, 7, 8, 12)
    jup_aspects_mars = ("Jupiter", "Mars") in full_aspect_pairs or jup["house"] == mars_h
    mars_own_exalt = mars["dignity"] in ("EXALTED", "MOOLATRIKONA", "OWN_SIGN")
    if kuja_raw:
        if mars_own_exalt or jup_aspects_mars:
            kuja_status = "CANCELLED_OR_MITIGATED"
            kuja_strength = "MITIGATED"
            kuja_obs = [
                f"Mars occupies House {mars_h} ({mars['sign']}) — classical Kuja Dosha house",
                f"Kuja Dosha Parihara (Cancellation) active: {'Mars in Own/Exalted dignity' if mars_own_exalt else 'Mars aspected/conjoined by benefic Jupiter'}",
            ]
        else:
            kuja_status = "ACTIVE"
            kuja_strength = "MODERATE" if mars_h in (1, 2, 4, 12) else "STRONG"
            kuja_obs = [
                f"Mars occupies House {mars_h} ({mars['sign']}) without own-sign or Jupiter aspect cancellation"
            ]
    else:
        kuja_status = "NOT_FORMED"
        kuja_strength = "INACTIVE"
        kuja_obs = [f"Mars is in House {mars_h} ({mars['sign']}), outside 1, 2, 4, 7, 8, 12"]

    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="MANGAL_KUJA_DOSHA",
            name="Mangal / Kuja Dosha (with Parihara Check)",
            sanskrit_name="मंगल / कुज दोष",
            category="DOSHA",
            definition="Mars occupying 1st, 2nd, 4th, 7th, 8th, or 12th house from Ascendant, evaluated alongside classical BPHS/South Indian Parihara (cancellation) rules.",
            classical_Effect="Heightens martial intensity in partnerships; when cancelled/mitigated, channels energy into constructive ambition.",
            required_conditions=["Mars in House 1, 2, 4, 7, 8, or 12 from Lagna", "Checked against Own/Exalted dignity and Jupiter Drishti cancellation"],
            detected_conditions=kuja_obs,
            planets_involved=["Mars"],
            houses_involved=[mars_h],
            status=kuja_status,
            strength=kuja_strength,
            is_benefic=False,
        )
    )

    # Kala Sarpa Yoga (All 7 planets hemmed between Rahu and Ketu)
    rahu_lon = p_map["Rahu"]["longitude"]
    ketu_lon = p_map["Ketu"]["longitude"]
    seven_classical = ["Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"]
    side_a = all(0.0 < ((p_map[p]["longitude"] - rahu_lon) % 360.0) < 180.0 for p in seven_classical)
    side_b = all(0.0 < ((p_map[p]["longitude"] - ketu_lon) % 360.0) < 180.0 for p in seven_classical)
    ks_formed = side_a or side_b
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="KALA_SARPA_YOGA",
            name="Kala Sarpa Yoga",
            sanskrit_name="कालसर्प योग",
            category="DOSHA",
            definition="All 7 classical planets (Sun through Saturn) positioned within one 180° hemisphere between Rahu and Ketu.",
            classical_Effect="Produces intense karmic Turning points, unusual life trajectory, and deep spiritual awakening.",
            required_conditions=["All 7 planets from Sun to Saturn must lie on the same side of the Rahu–Ketu nodal axis"],
            detected_conditions=[
                f"Rahu at {rahu_lon:.2f}° (H{p_map['Rahu']['house']}) & Ketu at {ketu_lon:.2f}° (H{p_map['Ketu']['house']}) — {'All 7 planets hemmed on one arc ✓' if ks_formed else 'Planets distributed across both nodal hemispheres (Kala Sarpa not formed ✓)'}"
            ],
            planets_involved=["Rahu", "Ketu"],
            houses_involved=[p_map["Rahu"]["house"], p_map["Ketu"]["house"]],
            status="ACTIVE" if ks_formed else "NOT_FORMED",
            strength="STRONG" if ks_formed else "INACTIVE",
            is_benefic=False,
        )
    )

    # Guru Chandala Yoga (Jupiter conjunct Rahu or Ketu)
    gc_rahu = jup["house"] == p_map["Rahu"]["house"]
    gc_ketu = jup["house"] == p_map["Ketu"]["house"]
    gc_formed = gc_rahu or gc_ketu
    evaluated.append(
        YogaEvaluationEntry(
            yoga_code="GURU_CHANDALA_YOGA",
            name="Guru-Nodal Yoga (Guru Chandala / Ganesha Yoga)",
            sanskrit_name="गुरु-राहु / केतु योग",
            category="DOSHA",
            definition="Jupiter conjoined with Rahu or Ketu in the same house.",
            classical_Effect="Prompts unconventional philosophy, questioning of orthodoxy, or reformist spiritual insight.",
            required_conditions=["Jupiter in the same house as Rahu or Ketu"],
            detected_conditions=[
                f"Jupiter is in H{jup['house']}, Rahu in H{p_map['Rahu']['house']}, Ketu in H{p_map['Ketu']['house']}"
            ],
            planets_involved=["Jupiter"] + (["Rahu"] if gc_rahu else (["Ketu"] if gc_ketu else [])),
            houses_involved=[jup["house"]],
            status="ACTIVE" if gc_formed else "NOT_FORMED",
            strength="MODERATE" if gc_formed else "INACTIVE",
            is_benefic=False,
        )
    )

    active_yogas = [
        y for y in evaluated if y.status in ("ACTIVE", "CANCELLED_OR_MITIGATED")
    ]
    active_yoga_count = sum(
        1 for y in evaluated if y.status == "ACTIVE" and y.is_benefic
    )
    active_dosha_count = sum(
        1 for y in evaluated if y.status == "ACTIVE" and not y.is_benefic
    )
    mitigated_count = sum(
        1 for y in evaluated if y.status == "CANCELLED_OR_MITIGATED"
    )

    return YogaCalculationResponse(
        utc_datetime_iso=resolved_time.utc_datetime_iso,
        julian_day_ut=planet_res.julian_day_ut,
        ascendant_sign=planet_res.ascendant_sign,
        moon_sign=moon["sign"],
        ayanamsha_type=planet_res.ayanamsha_type,
        active_yoga_count=active_yoga_count,
        active_dosha_count=active_dosha_count,
        mitigated_count=mitigated_count,
        total_evaluated_count=len(evaluated),
        active_yogas=active_yogas,
        all_evaluated_yogas=evaluated,
    )
