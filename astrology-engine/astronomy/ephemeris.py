"""
Swiss Ephemeris Sidereal Astronomical Wrapper.
Computes exact sidereal planetary positions, Ayanamsha, and Ascendant (Lagna).
"""

from dataclasses import dataclass
from functools import lru_cache
from typing import Dict
import swisseph as swe

AYANAMSHA_MODES: Dict[str, int] = {
    "LAHIRI": swe.SIDM_LAHIRI,
    "TRUE_CITRA": swe.SIDM_TRUE_CITRA,
    "KRISHNAMURTI": swe.SIDM_KRISHNAMURTI,
    "RAMAN": swe.SIDM_RAMAN,
}

SWE_PLANET_IDS: Dict[str, int] = {
    "Sun": swe.SUN,
    "Moon": swe.MOON,
    "Mars": swe.MARS,
    "Mercury": swe.MERCURY,
    "Jupiter": swe.JUPITER,
    "Venus": swe.VENUS,
    "Saturn": swe.SATURN,
}


@dataclass(frozen=True)
class RawEphemerisBody:
    name: str
    longitude: float
    latitude: float
    distance_au: float
    speed_longitude: float


@lru_cache(maxsize=2048)
def get_ayanamsha_value(jd_ut: float, ayanamsha_type: str = "LAHIRI") -> float:
    """Return exact sidereal Ayanamsha offset in degrees for the given Julian Day UT."""
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    return float(swe.get_ayanamsa_ut(jd_ut))


@lru_cache(maxsize=2048)
def _calculate_sidereal_ascendant_cached(
    jd_ut: float,
    latitude: float,
    longitude: float,
    ayanamsha_type: str = "LAHIRI",
) -> tuple[float, tuple[float, ...]]:
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    cusps_trop, ascmc_trop = swe.houses_ex(jd_ut, latitude, longitude, b"O", swe.FLG_SIDEREAL)
    asc_sidereal = float(ascmc_trop[0]) % 360.0
    if len(cusps_trop) == 13:
        sidereal_cusps = tuple(float(cusps_trop[i]) % 360.0 for i in range(1, 13))
    else:
        sidereal_cusps = tuple(float(cusps_trop[i]) % 360.0 for i in range(12))
    return asc_sidereal, sidereal_cusps


def calculate_sidereal_ascendant(
    jd_ut: float,
    latitude: float,
    longitude: float,
    ayanamsha_type: str = "LAHIRI",
) -> tuple[float, list[float]]:
    """
    Calculate exact sidereal Ascendant (Lagna) degree and 12 Sripati/Porphyry house cusps.
    Returns (sidereal_ascendant_deg, [12 sidereal cusp degrees]).
    """
    asc, cusps = _calculate_sidereal_ascendant_cached(jd_ut, latitude, longitude, ayanamsha_type)
    return asc, list(cusps)


@lru_cache(maxsize=4096)
def _calculate_raw_sidereal_bodies_cached(
    jd_ut: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
) -> tuple[tuple[str, RawEphemerisBody], ...]:
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    flags = swe.FLG_SIDEREAL | swe.FLG_SPEED

    items: list[tuple[str, RawEphemerisBody]] = []

    for name, body_id in SWE_PLANET_IDS.items():
        xx, _ = swe.calc_ut(jd_ut, body_id, flags)
        lon = float(xx[0]) % 360.0
        lat = float(xx[1])
        dist = float(xx[2])
        speed = float(xx[3])
        items.append((
            name,
            RawEphemerisBody(
                name=name,
                longitude=lon,
                latitude=lat,
                distance_au=dist,
                speed_longitude=speed,
            ),
        ))

    node_id = swe.TRUE_NODE if node_type.upper() == "TRUE_NODE" else swe.MEAN_NODE
    rahu_xx, _ = swe.calc_ut(jd_ut, node_id, flags)
    rahu_lon = float(rahu_xx[0]) % 360.0
    rahu_lat = float(rahu_xx[1])
    rahu_dist = float(rahu_xx[2])
    rahu_speed = float(rahu_xx[3])
    if node_id == swe.MEAN_NODE and rahu_speed > 0:
        rahu_speed = -rahu_speed

    ketu_lon = (rahu_lon + 180.0) % 360.0

    items.append((
        "Rahu",
        RawEphemerisBody(
            name="Rahu",
            longitude=rahu_lon,
            latitude=rahu_lat,
            distance_au=rahu_dist,
            speed_longitude=rahu_speed,
        ),
    ))
    items.append((
        "Ketu",
        RawEphemerisBody(
            name="Ketu",
            longitude=ketu_lon,
            latitude=-rahu_lat,
            distance_au=rahu_dist,
            speed_longitude=rahu_speed,
        ),
    ))

    return tuple(items)


def calculate_raw_sidereal_bodies(
    jd_ut: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
) -> Dict[str, RawEphemerisBody]:
    """
    Calculate exact sidereal ecliptic positions for the 9 Vedic Grahas
    (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu).
    """
    return dict(_calculate_raw_sidereal_bodies_cached(jd_ut, ayanamsha_type, node_type))


def get_ephemeris_cache_stats() -> dict:
    """Return aggregated LRU cache statistics across all astronomical ephemeris functions."""
    ayanamsha_info = get_ayanamsha_value.cache_info()
    asc_info = _calculate_sidereal_ascendant_cached.cache_info()
    bodies_info = _calculate_raw_sidereal_bodies_cached.cache_info()

    total_hits = ayanamsha_info.hits + asc_info.hits + bodies_info.hits
    total_misses = ayanamsha_info.misses + asc_info.misses + bodies_info.misses
    total_size = ayanamsha_info.currsize + asc_info.currsize + bodies_info.currsize
    max_size = ayanamsha_info.maxsize + asc_info.maxsize + bodies_info.maxsize

    return {
        "total_hits": total_hits,
        "total_misses": total_misses,
        "current_size": total_size,
        "max_size": max_size,
        "hit_ratio": total_hits / (total_hits + total_misses) if (total_hits + total_misses) > 0 else 0.0,
        "caches": {
            "ayanamsha": {"hits": ayanamsha_info.hits, "misses": ayanamsha_info.misses, "size": ayanamsha_info.currsize},
            "ascendant": {"hits": asc_info.hits, "misses": asc_info.misses, "size": asc_info.currsize},
            "planetary_bodies": {"hits": bodies_info.hits, "misses": bodies_info.misses, "size": bodies_info.currsize},
        },
    }


def clear_ephemeris_cache() -> None:
    """Evict all cached astronomical ephemeris data."""
    get_ayanamsha_value.cache_clear()
    _calculate_sidereal_ascendant_cached.cache_clear()
    _calculate_raw_sidereal_bodies_cached.cache_clear()
