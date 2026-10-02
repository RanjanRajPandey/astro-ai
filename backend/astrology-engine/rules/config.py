"""
Astrological Specification Configuration (Locked in Phase 0).
All defaults trace directly to docs/ASTROLOGY_SPECIFICATION.md.
"""

import os
from dataclasses import dataclass


@dataclass(frozen=True)
class AstrologySpecConfig:
    zodiac_system: str = "SIDEREAL"
    ayanamsha: str = os.getenv("DEFAULT_AYANAMSHA", "LAHIRI")
    node_type: str = os.getenv("DEFAULT_NODE_TYPE", "MEAN_NODE")
    house_system: str = os.getenv("DEFAULT_HOUSE_SYSTEM", "WHOLE_SIGN_WITH_SRIPATI")
    dasha_year_days: float = float(os.getenv("DEFAULT_DASHA_YEAR_DAYS", "365.2425"))
    rahu_ketu_trinal_aspects: bool = (
        os.getenv("RAHU_KETU_TRINAL_ASPECTS", "false").lower() == "true"
    )
    specification_version: str = "1.0.0-BPHS-LAHIRI"


DEFAULT_SPEC = AstrologySpecConfig()
