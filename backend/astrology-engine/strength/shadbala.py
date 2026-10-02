import math
from datetime import time
from typing import Dict, List

from aspects.drishti import calculate_all_aspects
from divisional.shodashavarga import calculate_shodashavarga
from models.aspects import AspectCalculationRequest
from models.divisional import DivisionalCalculationRequest
from models.strength import (
    KalaBalaBreakdown,
    PlanetStrengthEntry,
    ShadbalaCalculationRequest,
    ShadbalaCalculationResponse,
    SthanaBalaBreakdown,
)
from planets.calculator import calculate_planetary_positions
from timezone.resolver import resolve_birth_timestamp

# BPHS Deep Exaltation (Parama Uchcha) Longitudes in Sidereal Degrees
DEEP_EXALTATION_DEG: Dict[str, float] = {
    "SUN": 10.0,       # Aries 10°
    "MOON": 33.0,      # Taurus 3°
    "MARS": 298.0,     # Capricorn 28°
    "MERCURY": 165.0,  # Virgo 15°
    "JUPITER": 95.0,   # Cancer 5°
    "VENUS": 357.0,    # Pisces 27°
    "SATURN": 200.0,   # Libra 20°
    "RAHU": 50.0,      # Taurus 20°
    "KETU": 230.0,     # Scorpio 20°
}

# BPHS Naisargika Bala (Natural Luminosity Strength in Virupas)
NAISARGIKA_BALA_VIRUPAS: Dict[str, float] = {
    "SUN": 60.00,
    "MOON": 51.43,
    "VENUS": 42.86,
    "JUPITER": 34.29,
    "MERCURY": 25.71,
    "MARS": 17.14,
    "SATURN": 8.57,
    "RAHU": 12.86,
    "KETU": 12.86,
}

# BPHS Minimum Required Shadbala in Rupas (60 Virupas = 1 Rupa)
REQUIRED_MINIMUM_RUPAS: Dict[str, float] = {
    "SUN": 6.5,
    "MOON": 6.0,
    "MARS": 5.0,
    "MERCURY": 7.0,
    "JUPITER": 6.5,
    "VENUS": 5.5,
    "SATURN": 5.0,
    "RAHU": 5.0,
    "KETU": 5.0,
}

# Classical BPHS Saptavargaja Virupa table per Varga
SAPTAVARGA_DIGNITY_VIRUPAS: Dict[str, float] = {
    "MOOLATRIKONA": 45.0,
    "EXALTED": 30.0,
    "OWN_SIGN": 30.0,
    "GREAT_FRIEND": 22.5,
    "FRIEND": 15.0,
    "NEUTRAL": 7.5,
    "ENEMY": 3.75,
    "GREAT_ENEMY": 1.875,
    "DEBILITATED": 1.875,
}

# Classical BPHS 16-Varga Shodashavarga Vimshopaka weights (Total = 20.0)
SHODASHAVARGA_VIMSHOPAKA_WEIGHTS: Dict[str, float] = {
    "D1": 3.5,
    "D2": 1.0,
    "D3": 1.0,
    "D4": 0.5,
    "D7": 0.5,
    "D9": 3.0,
    "D10": 0.5,
    "D12": 0.5,
    "D16": 2.0,
    "D20": 0.5,
    "D24": 0.5,
    "D27": 0.5,
    "D30": 1.0,
    "D40": 0.5,
    "D45": 0.5,
    "D60": 4.0,
}

VIMSHOPAKA_DIGNITY_FACTORS: Dict[str, float] = {
    "MOOLATRIKONA": 1.0,
    "EXALTED": 1.0,
    "OWN_SIGN": 0.9,
    "GREAT_FRIEND": 0.75,
    "FRIEND": 0.65,
    "NEUTRAL": 0.50,
    "ENEMY": 0.35,
    "GREAT_ENEMY": 0.25,
    "DEBILITATED": 0.20,
}

SAPTAVARGA_CODES = {"D1", "D2", "D3", "D7", "D9", "D12", "D30"}

WEEKDAY_LORDS = [
    "MOON",     # Monday (0)
    "MARS",     # Tuesday (1)
    "MERCURY",  # Wednesday (2)
    "JUPITER",  # Thursday (3)
    "VENUS",    # Friday (4)
    "SATURN",   # Saturday (5)
    "SUN",      # Sunday (6)
]


def _shortest_arc(deg_a: float, deg_b: float) -> float:
    diff = abs(deg_a - deg_b) % 360.0
    return 360.0 - diff if diff > 180.0 else diff


def compute_uchcha_bala(planet: str, longitude: float) -> float:
    uchcha = DEEP_EXALTATION_DEG.get(planet.upper(), 0.0)
    neecha = (uchcha + 180.0) % 360.0
    arc = _shortest_arc(longitude, neecha)
    return round(arc / 3.0, 3)


def compute_dig_bala(planet: str, planet_lon: float, asc_lon: float) -> float:
    """
    Calculates Directional Strength (Dig Bala) in Virupas (0 to 60).
    Powerless points (Digbala Sunya):
    - Jupiter, Mercury: 7th House cusp (asc + 180)
    - Sun, Mars: 4th House cusp (asc + 90)
    - Saturn, Rahu, Ketu: 1st House cusp (asc)
    - Moon, Venus: 10th House cusp (asc + 270)
    """
    u = planet.upper()
    if u in ("JUPITER", "MERCURY"):
        zero_point = (asc_lon + 180.0) % 360.0
    elif u in ("SUN", "MARS"):
        zero_point = (asc_lon + 90.0) % 360.0
    elif u in ("MOON", "VENUS"):
        zero_point = (asc_lon + 270.0) % 360.0
    else:  # SATURN, RAHU, KETU
        zero_point = asc_lon % 360.0

    arc = _shortest_arc(planet_lon, zero_point)
    return round(arc / 3.0, 3)


def calculate_shadbala_and_vimshopaka(
    request: ShadbalaCalculationRequest,
) -> ShadbalaCalculationResponse:
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

    shodasha_res = calculate_shodashavarga(
        DivisionalCalculationRequest(
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

    # Index varga placements by (varga_code, planet_upper)
    varga_lookup: Dict[tuple, object] = {}
    for vc in shodasha_res.charts:
        for vp in vc.planets:
            varga_lookup[(vc.varga_code, vp.planet.upper())] = vp

    asc_lon = float(planet_res.ascendant_longitude)
    ayanamsha_val = float(planet_res.ayanamsha_value)

    sun_lon = 0.0
    moon_lon = 0.0
    for p in planet_res.planets:
        if p.planet.upper() == "SUN":
            sun_lon = float(p.longitude)
        elif p.planet.upper() == "MOON":
            moon_lon = float(p.longitude)

    # Local time of day in hours (0..24) for Nathonnatha & Tribhaga Bala
    tob: time = request.time_of_birth if request.time_of_birth is not None else time(12, 0, 0)
    local_hours = tob.hour + (tob.minute / 60.0) + (tob.second / 3600.0)
    hours_from_midnight = min(local_hours, 24.0 - local_hours)  # 0 at midnight, 12 at noon
    nathonnatha_day_virupas = round((hours_from_midnight / 12.0) * 60.0, 3)
    nathonnatha_night_virupas = round(60.0 - nathonnatha_day_virupas, 3)

    # Tribhaga watch ruler
    if 6.0 <= local_hours < 18.0:
        day_third = int((local_hours - 6.0) // 4.0)
        tribhaga_ruler = ["MERCURY", "SUN", "SATURN"][min(2, max(0, day_third))]
    else:
        night_elapsed = (local_hours - 18.0) if local_hours >= 18.0 else (local_hours + 6.0)
        night_third = int(night_elapsed // 4.0)
        tribhaga_ruler = ["MOON", "VENUS", "MARS"][min(2, max(0, night_third))]

    weekday_lord = WEEKDAY_LORDS[request.date_of_birth.weekday()]
    moon_sun_arc = _shortest_arc(moon_lon, sun_lon)  # 0..180

    # Pre-accumulate Drik Bala for each target planet from Sphuta Drishti
    drik_bala_map: Dict[str, float] = {p.planet.upper(): 0.0 for p in planet_res.planets}
    for pa in aspect_res.planet_aspects:
        tgt_u = pa.target_planet.upper()
        contrib = pa.sphuta_virupa_strength / 4.0
        if pa.aspect_nature == "BENEFIC":
            drik_bala_map[tgt_u] = drik_bala_map.get(tgt_u, 0.0) + contrib
        else:
            drik_bala_map[tgt_u] = drik_bala_map.get(tgt_u, 0.0) - contrib

    raw_entries: List[PlanetStrengthEntry] = []

    for p in planet_res.planets:
        u_name = p.planet.upper()
        lon = float(p.longitude)
        house_num = int(p.house)

        # --- 1. STHANA BALA ---
        uchcha_bala = compute_uchcha_bala(u_name, lon)

        # Saptavargaja Bala (sum across D1, D2, D3, D7, D9, D12, D30)
        saptavargaja = 0.0
        for v_code in SAPTAVARGA_CODES:
            vp = varga_lookup.get((v_code, u_name))
            dig = vp.dignity_in_varga if vp is not None else p.dignity
            saptavargaja += SAPTAVARGA_DIGNITY_VIRUPAS.get(dig, 7.5)
        saptavargaja = round(saptavargaja, 3)

        # Ojhayugmarasyamsa Bala (D1 & D9 odd/even sign parity)
        d1_is_even = (int(p.sign_index) % 2) == 0
        d9_vp = varga_lookup.get(("D9", u_name))
        d9_is_even = (int(d9_vp.varga_sign_index) % 2) == 0 if d9_vp is not None else d1_is_even
        ojha_bala = 0.0
        if u_name in ("MOON", "VENUS"):
            if d1_is_even:
                ojha_bala += 15.0
            if d9_is_even:
                ojha_bala += 15.0
        else:
            if not d1_is_even:
                ojha_bala += 15.0
            if not d9_is_even:
                ojha_bala += 15.0

        # Kendradi Bala
        if house_num in (1, 4, 7, 10):
            kendradi_bala = 60.0
        elif house_num in (2, 5, 8, 11):
            kendradi_bala = 30.0
        else:
            kendradi_bala = 15.0

        # Drekkana Bala
        deg_in_sign = lon % 30.0
        decanate = int(deg_in_sign // 10.0) + 1  # 1, 2, or 3
        drekkana_bala = 0.0
        if u_name in ("SUN", "MARS", "JUPITER") and decanate == 1:
            drekkana_bala = 15.0
        elif u_name in ("MERCURY", "SATURN", "RAHU", "KETU") and decanate == 2:
            drekkana_bala = 15.0
        elif u_name in ("MOON", "VENUS") and decanate == 3:
            drekkana_bala = 15.0

        sthana_total = round(
            uchcha_bala + saptavargaja + ojha_bala + kendradi_bala + drekkana_bala, 3
        )
        sthana_breakdown = SthanaBalaBreakdown(
            uchcha_bala=uchcha_bala,
            saptavargaja_bala=saptavargaja,
            ojhayugmarasyamsa_bala=ojha_bala,
            kendradi_bala=kendradi_bala,
            drekkana_bala=drekkana_bala,
            total=sthana_total,
        )

        # --- 2. DIG BALA ---
        dig_bala = compute_dig_bala(u_name, lon, asc_lon)

        # --- 3. KALA BALA ---
        if u_name == "MERCURY":
            nathonnatha = 60.0
        elif u_name in ("SUN", "JUPITER", "VENUS"):
            nathonnatha = nathonnatha_day_virupas
        else:
            nathonnatha = nathonnatha_night_virupas

        if u_name in ("JUPITER", "VENUS", "MERCURY"):
            paksha_bala = round(moon_sun_arc / 3.0, 3)
        elif u_name == "MOON":
            # Moon's Paksha Bala is doubled in BPHS
            paksha_bala = round((moon_sun_arc / 3.0) * 2.0, 3)
        else:
            paksha_bala = round((180.0 - moon_sun_arc) / 3.0, 3)

        tribhaga_bala = (
            60.0 if (u_name == "JUPITER" or u_name == tribhaga_ruler) else 0.0
        )
        vara_bala = 45.0 if u_name == weekday_lord else 15.0

        # Ayana Bala (Equinoctial strength from tropical longitude sine)
        trop_lon_rad = math.radians((lon + ayanamsha_val) % 360.0)
        sin_decl = math.sin(trop_lon_rad)  # -1..+1
        if u_name == "MERCURY":
            ayana_bala = round(30.0 + abs(sin_decl) * 30.0, 3)
        elif u_name in ("SUN", "MARS", "JUPITER", "VENUS"):
            ayana_bala = round(30.0 + sin_decl * 30.0, 3)
        else:  # MOON, SATURN, RAHU, KETU
            ayana_bala = round(30.0 - sin_decl * 30.0, 3)

        kala_total = round(
            nathonnatha + paksha_bala + tribhaga_bala + vara_bala + ayana_bala, 3
        )
        kala_breakdown = KalaBalaBreakdown(
            nathonnatha_bala=nathonnatha,
            paksha_bala=paksha_bala,
            tribhaga_bala=tribhaga_bala,
            vara_bala=vara_bala,
            ayana_bala=ayana_bala,
            total=kala_total,
        )

        # --- 4. CHESTA BALA ---
        if u_name == "SUN":
            chesta_bala = ayana_bala
        elif u_name == "MOON":
            chesta_bala = min(60.0, paksha_bala)
        elif u_name in ("RAHU", "KETU"):
            chesta_bala = 45.0
        else:
            if p.is_retrograde:
                chesta_bala = 60.0
            else:
                elong_from_sun = _shortest_arc(lon, sun_lon)
                chesta_bala = round(15.0 + (elong_from_sun / 180.0) * 35.0, 3)

        # --- 5. NAISARGIKA BALA ---
        naisargika_bala = NAISARGIKA_BALA_VIRUPAS.get(u_name, 15.0)

        # --- 6. DRIK BALA ---
        # Shift baseline by +15 Virupas so total Shadbala is always positive while preserving Benefic/Malefic aspect differential
        drik_bala = round(drik_bala_map.get(u_name, 0.0), 3)

        total_virupas = round(
            max(
                60.0,
                sthana_total
                + dig_bala
                + kala_total
                + chesta_bala
                + naisargika_bala
                + drik_bala,
            ),
            3,
        )
        total_rupas = round(total_virupas / 60.0, 3)
        req_rupas = REQUIRED_MINIMUM_RUPAS.get(u_name, 5.0)
        ratio = round(total_rupas / req_rupas, 3)

        if ratio >= 1.25:
            grade = "VERY_STRONG"
        elif ratio >= 1.00:
            grade = "ADEQUATE"
        elif ratio >= 0.85:
            grade = "MODERATE"
        else:
            grade = "WEAK"

        # --- VIMSHOPAKA BALA (16-Varga Shodashavarga out of 20.0) ---
        vimshopaka = 0.0
        for v_code, weight in SHODASHAVARGA_VIMSHOPAKA_WEIGHTS.items():
            vp = varga_lookup.get((v_code, u_name))
            dig = vp.dignity_in_varga if vp is not None else p.dignity
            factor = VIMSHOPAKA_DIGNITY_FACTORS.get(dig, 0.50)
            vimshopaka += weight * factor
        vimshopaka = round(min(20.0, max(0.0, vimshopaka)), 3)
        vimshopaka_pct = round((vimshopaka / 20.0) * 100.0, 2)

        raw_entries.append(
            PlanetStrengthEntry(
                planet=p.planet,
                sign=p.sign,
                house=house_num,
                d1_dignity=p.dignity,
                is_retrograde=p.is_retrograde,
                sthana_bala=sthana_total,
                sthana_breakdown=sthana_breakdown,
                dig_bala=dig_bala,
                kala_bala=kala_total,
                kala_breakdown=kala_breakdown,
                chesta_bala=chesta_bala,
                naisargika_bala=naisargika_bala,
                drik_bala=drik_bala,
                total_shadbala_virupas=total_virupas,
                total_shadbala_rupas=total_rupas,
                required_minimum_rupas=req_rupas,
                shadbala_ratio=ratio,
                vimshopaka_bala=vimshopaka,
                vimshopaka_percentage=vimshopaka_pct,
                strength_grade=grade,
                rank=1,
            )
        )

    # Assign ranks 1..9 by shadbala_ratio descending
    sorted_by_ratio = sorted(raw_entries, key=lambda x: x.shadbala_ratio, reverse=True)
    rank_map = {entry.planet: idx + 1 for idx, entry in enumerate(sorted_by_ratio)}

    final_entries = [
        entry.model_copy(update={"rank": rank_map[entry.planet]})
        for entry in raw_entries
    ]

    return ShadbalaCalculationResponse(
        utc_datetime_iso=resolved_time.utc_datetime_iso,
        julian_day_ut=planet_res.julian_day_ut,
        ascendant_sign=planet_res.ascendant_sign,
        ayanamsha_type=planet_res.ayanamsha_type,
        strongest_planet=sorted_by_ratio[0].planet,
        weakest_planet=sorted_by_ratio[-1].planet,
        planets=final_entries,
    )
