"""
Deterministic 27-Nakshatra, 4-Pada, Classical Metadata, and 9-Fold Tara Bala Engine.
Provides complete Nakshatra data to the Dasha Engine, Chart Engine, Analysis Engine, and AI Engine.
"""

from dataclasses import dataclass
from typing import Dict, List
from planets.calculator import calculate_planetary_positions, format_sign_dms
from rules.vedic_constants import ZODIAC_SIGNS


@dataclass(frozen=True)
class NakshatraMetadata:
    index: int  # 1..27
    name: str
    ruler: str
    deity: str
    gana: str
    nadi: str
    yoni: str
    symbol: str


# Canonical 27 Nakshatra Table (BPHS & Muhurta Chintamani)
NAKSHATRA_CATALOG: List[NakshatraMetadata] = [
    NakshatraMetadata(1, "Ashwini", "Ketu", "Ashwini Kumaras", "Deva", "Adi", "Horse", "Horse's Head"),
    NakshatraMetadata(2, "Bharani", "Venus", "Yama", "Manushya", "Madhya", "Elephant", "Yoni"),
    NakshatraMetadata(3, "Krittika", "Sun", "Agni", "Rakshasa", "Antya", "Sheep", "Razor / Flame"),
    NakshatraMetadata(4, "Rohini", "Moon", "Prajapati (Brahma)", "Manushya", "Antya", "Serpent", "Chariot / Ox Cart"),
    NakshatraMetadata(5, "Mrigashira", "Mars", "Soma (Chandra)", "Deva", "Madhya", "Serpent", "Deer's Head"),
    NakshatraMetadata(6, "Ardra", "Rahu", "Rudra", "Manushya", "Adi", "Dog", "Teardrop / Diamond"),
    NakshatraMetadata(7, "Punarvasu", "Jupiter", "Aditi", "Deva", "Adi", "Cat", "Quiver of Arrows"),
    NakshatraMetadata(8, "Pushya", "Saturn", "Brihaspati", "Deva", "Madhya", "Sheep", "Cow's Udder / Lotus"),
    NakshatraMetadata(9, "Ashlesha", "Mercury", "Nagas (Sarpas)", "Rakshasa", "Antya", "Cat", "Coiled Serpent"),
    NakshatraMetadata(10, "Magha", "Ketu", "Pitris (Ancestors)", "Rakshasa", "Antya", "Rat", "Royal Throne"),
    NakshatraMetadata(11, "Purva Phalguni", "Venus", "Bhaga", "Manushya", "Madhya", "Rat", "Front Legs of Bed"),
    NakshatraMetadata(12, "Uttara Phalguni", "Sun", "Aryaman", "Manushya", "Adi", "Cow", "Back Legs of Bed"),
    NakshatraMetadata(13, "Hasta", "Moon", "Savitur (Surya)", "Deva", "Adi", "Buffalo", "Open Hand / Palm"),
    NakshatraMetadata(14, "Chitra", "Mars", "Vishwakarma (Tvashtar)", "Rakshasa", "Madhya", "Tiger", "Shining Jewel"),
    NakshatraMetadata(15, "Swati", "Rahu", "Vayu", "Deva", "Antya", "Buffalo", "Young Shoot / Coral"),
    NakshatraMetadata(16, "Vishakha", "Jupiter", "Indragni", "Rakshasa", "Antya", "Tiger", "Triumphal Arch"),
    NakshatraMetadata(17, "Anuradha", "Saturn", "Mitra", "Deva", "Madhya", "Deer", "Lotus Flower"),
    NakshatraMetadata(18, "Jyeshtha", "Mercury", "Indra", "Rakshasa", "Adi", "Deer", "Circular Amulet / Umbrella"),
    NakshatraMetadata(19, "Moola", "Ketu", "Nirriti", "Rakshasa", "Adi", "Dog", "Tied Bunch of Roots"),
    NakshatraMetadata(20, "Purva Ashadha", "Venus", "Apah (Cosmic Waters)", "Manushya", "Madhya", "Monkey", "Winnowing Basket / Fan"),
    NakshatraMetadata(21, "Uttara Ashadha", "Sun", "Vishvadevas", "Manushya", "Antya", "Mongoose", "Elephant's Tusk"),
    NakshatraMetadata(22, "Shravana", "Moon", "Vishnu", "Deva", "Antya", "Monkey", "Ear / Three Footprints"),
    NakshatraMetadata(23, "Dhanishta", "Mars", "Ashta Vasus", "Rakshasa", "Madhya", "Lion", "Musical Drum (Mridanga)"),
    NakshatraMetadata(24, "Shatabhisha", "Rahu", "Varuna", "Rakshasa", "Adi", "Horse", "Empty Circle / 100 Stars"),
    NakshatraMetadata(25, "Purva Bhadrapada", "Jupiter", "Aja Ekapada", "Manushya", "Adi", "Lion", "Sword / Front of Funeral Cot"),
    NakshatraMetadata(26, "Uttara Bhadrapada", "Saturn", "Ahirbudhnya", "Manushya", "Madhya", "Cow", "Twins / Back of Cot"),
    NakshatraMetadata(27, "Revati", "Mercury", "Pushan", "Deva", "Antya", "Elephant", "Fish / Drum"),
]

TARA_BALA_NAMES: Dict[int, tuple[str, str]] = {
    1: ("JANMA", "NEUTRAL"),
    2: ("SAMPAT", "SUPPORTING"),
    3: ("VIPAT", "CHALLENGING"),
    4: ("KSHEMA", "SUPPORTING"),
    5: ("PRATYAK", "CHALLENGING"),
    6: ("SADHANA", "SUPPORTING"),
    7: ("NAIDHANA", "CHALLENGING"),
    8: ("MITRA", "SUPPORTING"),
    9: ("PARAMA_MITRA", "SUPPORTING"),
}


@dataclass(frozen=True)
class CalculatedNakshatraPlacement:
    body_name: str
    longitude: float
    rashi_sign: str
    nakshatra_name: str
    nakshatra_index: int  # 1..27
    pada: int  # 1..4
    pada_navamsha_sign: str
    ruler_planet: str
    degree_in_nakshatra: float
    degree_in_nakshatra_dms: str
    elapsed_fraction: float
    remaining_fraction: float
    deity: str
    gana: str
    nadi: str
    yoni: str
    symbol: str
    tara_number_from_moon: int
    tara_name_from_moon: str
    tara_quality: str
    relationship_to_nakshatra_lord: str


@dataclass(frozen=True)
class NakshatraEngineResult:
    julian_day_ut: float
    ayanamsha_type: str
    ayanamsha_value: float
    janma_nakshatra: str
    janma_nakshatra_index: int
    janma_pada: int
    janma_nakshatra_lord: str
    janma_rashi: str
    moon_elapsed_fraction: float
    moon_remaining_fraction: float
    placements: List[CalculatedNakshatraPlacement]


def compute_single_nakshatra_placement(
    body_name: str,
    longitude: float,
    moon_nakshatra_index_1: int,
    relationship_to_ruler: str,
) -> CalculatedNakshatraPlacement:
    lon = longitude % 360.0
    rashi_idx_0 = int(lon // 30.0) % 12
    rashi_sign = ZODIAC_SIGNS[rashi_idx_0]

    nak_span = 360.0 / 27.0  # 13.333333333333334 degrees
    pada_span = nak_span / 4.0  # 3.3333333333333335 degrees

    nak_idx_0 = min(26, int(lon // nak_span))
    meta = NAKSHATRA_CATALOG[nak_idx_0]

    deg_in_nak = lon - (nak_idx_0 * nak_span)
    pada = min(4, int(deg_in_nak // pada_span) + 1)

    # Every pada (3°20') corresponds to 1 Navamsha sign sequentially from 0° Aries (108 padas = 9 full zodiac cycles)
    global_pada_idx_0 = (nak_idx_0 * 4) + (pada - 1)
    pada_navamsha_sign = ZODIAC_SIGNS[global_pada_idx_0 % 12]

    elapsed_frac = max(0.0, min(1.0, deg_in_nak / nak_span))
    remaining_frac = max(0.0, min(1.0, 1.0 - elapsed_frac))

    # Tara Bala from Moon's Janma Nakshatra (1..9 cycle)
    distance_from_moon = ((meta.index - moon_nakshatra_index_1) % 27) + 1
    tara_num = ((distance_from_moon - 1) % 9) + 1
    tara_name, tara_quality = TARA_BALA_NAMES[tara_num]

    return CalculatedNakshatraPlacement(
        body_name=body_name,
        longitude=round(lon, 6),
        rashi_sign=rashi_sign,
        nakshatra_name=meta.name,
        nakshatra_index=meta.index,
        pada=pada,
        pada_navamsha_sign=pada_navamsha_sign,
        ruler_planet=meta.ruler,
        degree_in_nakshatra=round(deg_in_nak, 6),
        degree_in_nakshatra_dms=format_sign_dms(deg_in_nak),
        elapsed_fraction=round(elapsed_frac, 8),
        remaining_fraction=round(remaining_frac, 8),
        deity=meta.deity,
        gana=meta.gana,
        nadi=meta.nadi,
        yoni=meta.yoni,
        symbol=meta.symbol,
        tara_number_from_moon=tara_num,
        tara_name_from_moon=tara_name,
        tara_quality=tara_quality,
        relationship_to_nakshatra_lord=relationship_to_ruler,
    )


def calculate_nakshatras(
    jd_ut: float,
    latitude: float,
    longitude: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
) -> NakshatraEngineResult:
    """
    Calculate complete Nakshatra & Pada placements for Ascendant + all 9 Grahas.
    """
    planetary_res = calculate_planetary_positions(
        jd_ut=jd_ut,
        latitude=latitude,
        longitude=longitude,
        ayanamsha_type=ayanamsha_type,
        node_type=node_type,
    )

    planets_by_name = {p.planet: p for p in planetary_res.planets}
    moon_planet = planets_by_name["Moon"]
    moon_nak_idx_1 = moon_planet.nakshatra_index

    placements: List[CalculatedNakshatraPlacement] = []

    # 1. Ascendant (Lagna) Nakshatra Placement
    asc_nak_idx_0 = min(26, int((planetary_res.ascendant_longitude % 360.0) // (360.0 / 27.0)))
    asc_nak_ruler = NAKSHATRA_CATALOG[asc_nak_idx_0].ruler
    asc_lord = planetary_res.planets[0]  # fallback
    for p in planetary_res.planets:
        if p.sign == planetary_res.ascendant_sign:
            asc_lord = p
            break
    asc_rel = "SELF" if asc_nak_ruler == asc_lord.sign_lord else "NEUTRAL"

    placements.append(
        compute_single_nakshatra_placement(
            body_name="Ascendant",
            longitude=planetary_res.ascendant_longitude,
            moon_nakshatra_index_1=moon_nak_idx_1,
            relationship_to_ruler=asc_rel,
        )
    )

    # 2. All 9 Grahas
    for p in planetary_res.planets:
        nak_ruler = p.nakshatra_lord
        if nak_ruler == p.planet:
            rel_to_ruler = "OWN_STAR"
        else:
            rel_to_ruler = p.planetary_relationships[nak_ruler]["compound"]

        placements.append(
            compute_single_nakshatra_placement(
                body_name=p.planet,
                longitude=p.longitude,
                moon_nakshatra_index_1=moon_nak_idx_1,
                relationship_to_ruler=rel_to_ruler,
            )
        )

    moon_placement = next(pl for pl in placements if pl.body_name == "Moon")

    return NakshatraEngineResult(
        julian_day_ut=planetary_res.julian_day_ut,
        ayanamsha_type=planetary_res.ayanamsha_type,
        ayanamsha_value=planetary_res.ayanamsha_value,
        janma_nakshatra=moon_placement.nakshatra_name,
        janma_nakshatra_index=moon_placement.nakshatra_index,
        janma_pada=moon_placement.pada,
        janma_nakshatra_lord=moon_placement.ruler_planet,
        janma_rashi=moon_placement.rashi_sign,
        moon_elapsed_fraction=moon_placement.elapsed_fraction,
        moon_remaining_fraction=moon_placement.remaining_fraction,
        placements=placements,
    )
