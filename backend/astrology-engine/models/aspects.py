from datetime import date, time
from typing import List, Optional
from pydantic import BaseModel, Field


class HouseAspectEntry(BaseModel):
    """Represents a planet's aspect (Drishti) onto a specific house (Bhava)."""
    source_planet: str
    source_sign: str
    source_house: int = Field(..., ge=1, le=12)
    target_house: int = Field(..., ge=1, le=12)
    target_sign: str
    house_offset: int = Field(..., ge=1, le=12, description="1-indexed count from source_house to target_house")
    aspect_type: str = Field(..., description="e.g., 7TH_FULL, 4TH_SPECIAL_MARS, 8TH_SPECIAL_MARS, 5TH_SPECIAL_JUPITER, 9TH_SPECIAL_JUPITER, 3RD_SPECIAL_SATURN, 10TH_SPECIAL_SATURN, 5TH_TRINAL_NODE, 9TH_TRINAL_NODE, PADA_3_4_4TH, etc.")
    is_full_aspect: bool = Field(..., description="True for 7th house and planet-specific Vishesha (special) full aspects (60 Virupas)")
    is_special_aspect: bool = Field(..., description="True for Mars 4/8, Jupiter 5/9, Saturn 3/10, or Rahu/Ketu 5/9 special aspects")
    pada_fraction: str = Field(..., description="4/4 (Full), 3/4, 1/2, or 1/4")
    virupa_strength: float = Field(..., ge=0.0, le=60.0, description="Parashari Drishti strength in Virupas (60 = 1 Rupa / Full)")
    aspect_nature: str = Field(..., description="BENEFIC or MALEFIC based on natural benefic/malefic nature of source planet")
    rule_applied: str


class PlanetToPlanetAspect(BaseModel):
    """Represents a directed aspect (Graha Drishti & Sphuta Drishti) from source_planet to target_planet."""
    source_planet: str
    source_house: int = Field(..., ge=1, le=12)
    source_sign: str
    source_longitude: float
    target_planet: str
    target_house: int = Field(..., ge=1, le=12)
    target_sign: str
    target_longitude: float
    house_offset: int = Field(..., ge=1, le=12)
    angular_separation_deg: float = Field(..., ge=0.0, lt=360.0, description="Forward zodiacal arc (target_lon - source_lon) mod 360")
    orb_from_exact_aspect_deg: float = Field(..., ge=0.0, description="Absolute degree difference from exact aspect angle")
    aspect_type: str
    is_full_aspect: bool
    is_special_aspect: bool
    pada_fraction: str
    virupa_strength: float = Field(..., ge=0.0, le=60.0, description="House-based Parashari Virupa strength (15, 30, 45, or 60)")
    sphuta_virupa_strength: float = Field(..., ge=0.0, le=60.0, description="Continuous degree-based BPHS Sphuta Drishti Virupa strength (0 to 60)")
    aspect_nature: str = Field(..., description="BENEFIC or MALEFIC")
    rule_applied: str


class MutualAspectSummary(BaseModel):
    """Represents a mutual relationship (Paraspara Drishti or Yuti Conjunction) between two planets."""
    planet_a: str
    house_a: int
    sign_a: str
    planet_b: str
    house_b: int
    sign_b: str
    relationship_type: str = Field(..., description="CONJUNCTION_YUTI, MUTUAL_7TH_OPPOSITION, or MUTUAL_SPECIAL_LOCK")
    a_to_b_aspect_type: str
    b_to_a_aspect_type: str
    combined_virupa_strength: float
    exact_orb_deg: float
    description: str


class AspectCalculationRequest(BaseModel):
    """Request payload for calculating planetary & house aspects (Drishti)."""
    date_of_birth: date
    time_of_birth: Optional[time] = None
    latitude: float = Field(..., ge=-90.0, le=90.0)
    longitude: float = Field(..., ge=-180.0, le=180.0)
    timezone_id: Optional[str] = None
    ayanamsha_type: str = "LAHIRI"
    node_type: str = "MEAN_NODE"
    rahu_ketu_trinal_aspects: bool = Field(default=True, description="Include 5th and 9th trinal aspects for Rahu and Ketu")
    include_pada_drishti: bool = Field(default=True, description="Include partial 1/4, 1/2, 3/4 Parashari aspects in addition to Full aspects")


class AspectCalculationResponse(BaseModel):
    """Complete Phase 9 Drishti response."""
    utc_datetime_iso: str
    julian_day_ut: float
    ascendant_sign: str
    ayanamsha_type: str
    rahu_ketu_trinal_aspects: bool
    house_aspects: List[HouseAspectEntry]
    planet_aspects: List[PlanetToPlanetAspect]
    mutual_relationships: List[MutualAspectSummary]
