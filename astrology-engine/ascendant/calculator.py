"""
Deterministic Ascendant (Udaya Lagna) and Special Lagnas Calculator.
Computes Udaya Lagna, Chandra Lagna, Surya Lagna, Arudha Lagna (AL), and Upapada Lagna (UL).
"""

from dataclasses import dataclass
from typing import Dict
from planets.calculator import CalculatedPlanet, format_sign_dms
from rules.vedic_constants import (
    ZODIAC_SIGNS,
    SANSKRIT_SIGN_NAMES,
    SIGN_LORDS,
    NAKSHATRA_LIST,
)


@dataclass(frozen=True)
class AscendantDetails:
    longitude: float
    sign: str
    sanskrit_sign: str
    sign_index: int  # 1..12
    degree_in_sign: float
    degree_dms: str
    nakshatra: str
    nakshatra_index: int  # 1..27
    nakshatra_lord: str
    pada: int  # 1..4
    lagna_lord: str
    lagna_lord_sign: str
    lagna_lord_house: int
    lagna_lord_dignity: str
    chandra_lagna_sign: str
    surya_lagna_sign: str
    arudha_lagna_sign: str
    arudha_lagna_house: int
    upapada_lagna_sign: str
    upapada_lagna_house: int


def compute_arudha_pada_sign_index(
    house_sign_idx_0: int, lord_sign_idx_0: int
) -> int:
    """
    Compute Arudha Pada sign index (0..11) per Brihat Parashara Hora Shastra (BPHS Ch. 29):
    1. Count distance from the house sign to its lord's sign: dist = (lord - house) % 12.
    2. Count the same distance from the lord's sign: raw_pada = (lord + dist) % 12.
    3. Classical Swastika Exception: If the resulting Arudha falls in the same sign (1st)
       or the 7th sign from the original house, advance by 10 signs (9 steps forward in 0-indexed math: +9 mod 12).
    """
    dist = (lord_sign_idx_0 - house_sign_idx_0) % 12
    raw_pada = (lord_sign_idx_0 + dist) % 12
    relative_house = ((raw_pada - house_sign_idx_0) % 12) + 1
    if relative_house in (1, 7):
        raw_pada = (raw_pada + 9) % 12
    return raw_pada


def calculate_ascendant_details(
    asc_longitude: float,
    planets_by_name: Dict[str, CalculatedPlanet],
) -> AscendantDetails:
    """Compute complete Ascendant and Special Lagna details."""
    lon = asc_longitude % 360.0
    asc_sign_idx_0 = int(lon // 30.0) % 12
    sign_name = ZODIAC_SIGNS[asc_sign_idx_0]
    deg_in_sign = lon % 30.0

    nak_span = 360.0 / 27.0
    pada_span = nak_span / 4.0
    nak_idx_0 = min(26, int(lon // nak_span))
    nak_name, nak_lord = NAKSHATRA_LIST[nak_idx_0]
    pada = min(4, int((lon - nak_idx_0 * nak_span) // pada_span) + 1)

    lagna_lord = SIGN_LORDS[sign_name]
    lagna_lord_obj = planets_by_name[lagna_lord]

    # Arudha Lagna (AL): Pada of 1st house
    al_sign_idx_0 = compute_arudha_pada_sign_index(
        asc_sign_idx_0, lagna_lord_obj.sign_index - 1
    )
    al_house = ((al_sign_idx_0 - asc_sign_idx_0) % 12) + 1

    # Upapada Lagna (UL): Pada of 12th house (Marriage & spouse indicator)
    twelfth_sign_idx_0 = (asc_sign_idx_0 + 11) % 12
    twelfth_lord = SIGN_LORDS[ZODIAC_SIGNS[twelfth_sign_idx_0]]
    twelfth_lord_obj = planets_by_name[twelfth_lord]
    ul_sign_idx_0 = compute_arudha_pada_sign_index(
        twelfth_sign_idx_0, twelfth_lord_obj.sign_index - 1
    )
    ul_house = ((ul_sign_idx_0 - asc_sign_idx_0) % 12) + 1

    return AscendantDetails(
        longitude=round(lon, 6),
        sign=sign_name,
        sanskrit_sign=SANSKRIT_SIGN_NAMES[sign_name],
        sign_index=asc_sign_idx_0 + 1,
        degree_in_sign=round(deg_in_sign, 6),
        degree_dms=format_sign_dms(deg_in_sign),
        nakshatra=nak_name,
        nakshatra_index=nak_idx_0 + 1,
        nakshatra_lord=nak_lord,
        pada=pada,
        lagna_lord=lagna_lord,
        lagna_lord_sign=lagna_lord_obj.sign,
        lagna_lord_house=lagna_lord_obj.house,
        lagna_lord_dignity=lagna_lord_obj.dignity,
        chandra_lagna_sign=planets_by_name["Moon"].sign,
        surya_lagna_sign=planets_by_name["Sun"].sign,
        arudha_lagna_sign=ZODIAC_SIGNS[al_sign_idx_0],
        arudha_lagna_house=al_house,
        upapada_lagna_sign=ZODIAC_SIGNS[ul_sign_idx_0],
        upapada_lagna_house=ul_house,
    )
