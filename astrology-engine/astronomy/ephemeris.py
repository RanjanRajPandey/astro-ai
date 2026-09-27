"""
Swiss Ephemeris Sidereal Astronomical Wrapper.
Computes exact sidereal planetary positions, Ayanamsha, and Ascendant (Lagna).
"""

from dataclasses import dataclass
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


def get_ayanamsha_value(jd_ut: float, ayanamsha_type: str = "LAHIRI") -> float:
    """Return exact sidereal Ayanamsha offset in degrees for the given Julian Day UT."""
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    return float(swe.get_ayanamsa_ut(jd_ut))


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
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    # Porphyry ('O') is the astronomical basis for Sripati Bhava cusps
    cusps_trop, ascmc_trop = swe.houses_ex(jd_ut, latitude, longitude, b"O", swe.FLG_SIDEREAL)
    asc_sidereal = float(ascmc_trop[0]) % 360.0
    # In pyswisseph, cusps_trop is a 12-tuple (indices 0..11) or 13-tuple depending on version
    if len(cusps_trop) == 13:
        sidereal_cusps = [float(cusps_trop[i]) % 360.0 for i in range(1, 13)]
    else:
        sidereal_cusps = [float(cusps_trop[i]) % 360.0 for i in range(12)]
    return asc_sidereal, sidereal_cusps


def calculate_raw_sidereal_bodies(
    jd_ut: float,
    ayanamsha_type: str = "LAHIRI",
    node_type: str = "MEAN_NODE",
) -> Dict[str, RawEphemerisBody]:
    """
    Calculate exact sidereal ecliptic positions for the 9 Vedic Grahas
    (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn, Rahu, Ketu).
    """
    mode = AYANAMSHA_MODES.get(ayanamsha_type.upper(), swe.SIDM_LAHIRI)
    swe.set_sid_mode(mode, 0, 0)
    flags = swe.FLG_SIDEREAL | swe.FLG_SPEED

    results: Dict[str, RawEphemerisBody] = {}

    for name, body_id in SWE_PLANET_IDS.items():
        xx, _ = swe.calc_ut(jd_ut, body_id, flags)
        lon = float(xx[0]) % 360.0
        lat = float(xx[1])
        dist = float(xx[2])
        speed = float(xx[3])
        results[name] = RawEphemerisBody(
            name=name,
            longitude=lon,
            latitude=lat,
            distance_au=dist,
            speed_longitude=speed,
        )

    # Lunar Nodes: Rahu & Ketu (Ketu is always 180 degrees opposite Rahu)
    node_id = swe.TRUE_NODE if node_type.upper() == "TRUE_NODE" else swe.MEAN_NODE
    rahu_xx, _ = swe.calc_ut(jd_ut, node_id, flags)
    rahu_lon = float(rahu_xx[0]) % 360.0
    rahu_lat = float(rahu_xx[1])
    rahu_dist = float(rahu_xx[2])
    rahu_speed = float(rahu_xx[3])
    # Mean node is always retrograde; ensure negative speed convention if mean node
    if node_id == swe.MEAN_NODE and rahu_speed > 0:
        rahu_speed = -rahu_speed

    ketu_lon = (rahu_lon + 180.0) % 360.0

    results["Rahu"] = RawEphemerisBody(
        name="Rahu",
        longitude=rahu_lon,
        latitude=rahu_lat,
        distance_au=rahu_dist,
        speed_longitude=rahu_speed,
    )
    results["Ketu"] = RawEphemerisBody(
        name="Ketu",
        longitude=ketu_lon,
        latitude=-rahu_lat,
        distance_au=rahu_dist,
        speed_longitude=rahu_speed,
    )

    return results
