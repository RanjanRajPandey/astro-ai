"""
Canonical Vedic Astrology (BPHS) Constants, Signs, Nakshatras, Dignities,
Combustion Orbs, and Natural Relationships (Naisargika Maitri).
Traces directly to docs/ASTROLOGY_SPECIFICATION.md.
"""

from typing import Dict, List, Set, Tuple

ZODIAC_SIGNS: List[str] = [
    "Aries",
    "Taurus",
    "Gemini",
    "Cancer",
    "Leo",
    "Virgo",
    "Libra",
    "Scorpio",
    "Sagittarius",
    "Capricorn",
    "Aquarius",
    "Pisces",
]

SANSKRIT_SIGN_NAMES: Dict[str, str] = {
    "Aries": "Mesha",
    "Taurus": "Vrishabha",
    "Gemini": "Mithuna",
    "Cancer": "Karka",
    "Leo": "Simha",
    "Virgo": "Kanya",
    "Libra": "Tula",
    "Scorpio": "Vrishchika",
    "Sagittarius": "Dhanu",
    "Capricorn": "Makara",
    "Aquarius": "Kumbha",
    "Pisces": "Meena",
}

SIGN_LORDS: Dict[str, str] = {
    "Aries": "Mars",
    "Taurus": "Venus",
    "Gemini": "Mercury",
    "Cancer": "Moon",
    "Leo": "Sun",
    "Virgo": "Mercury",
    "Libra": "Venus",
    "Scorpio": "Mars",
    "Sagittarius": "Jupiter",
    "Capricorn": "Saturn",
    "Aquarius": "Saturn",
    "Pisces": "Jupiter",
}

GRAHA_ORDER: List[str] = [
    "Sun",
    "Moon",
    "Mars",
    "Mercury",
    "Jupiter",
    "Venus",
    "Saturn",
    "Rahu",
    "Ketu",
]

# 27 Nakshatras with their Vimshottari ruler
NAKSHATRA_LIST: List[Tuple[str, str]] = [
    ("Ashwini", "Ketu"),
    ("Bharani", "Venus"),
    ("Krittika", "Sun"),
    ("Rohini", "Moon"),
    ("Mrigashira", "Mars"),
    ("Ardra", "Rahu"),
    ("Punarvasu", "Jupiter"),
    ("Pushya", "Saturn"),
    ("Ashlesha", "Mercury"),
    ("Magha", "Ketu"),
    ("Purva Phalguni", "Venus"),
    ("Uttara Phalguni", "Sun"),
    ("Hasta", "Moon"),
    ("Chitra", "Mars"),
    ("Swati", "Rahu"),
    ("Vishakha", "Jupiter"),
    ("Anuradha", "Saturn"),
    ("Jyeshtha", "Mercury"),
    ("Moola", "Ketu"),
    ("Purva Ashadha", "Venus"),
    ("Uttara Ashadha", "Sun"),
    ("Shravana", "Moon"),
    ("Dhanishta", "Mars"),
    ("Shatabhisha", "Rahu"),
    ("Purva Bhadrapada", "Jupiter"),
    ("Uttara Bhadrapada", "Saturn"),
    ("Revati", "Mercury"),
]

# Exaltation sign and deep exaltation degree (within sign)
EXALTATION_RULES: Dict[str, Tuple[str, float]] = {
    "Sun": ("Aries", 10.0),
    "Moon": ("Taurus", 3.0),
    "Mars": ("Capricorn", 28.0),
    "Mercury": ("Virgo", 15.0),
    "Jupiter": ("Cancer", 5.0),
    "Venus": ("Pisces", 27.0),
    "Saturn": ("Libra", 20.0),
    "Rahu": ("Taurus", 20.0),
    "Ketu": ("Scorpio", 20.0),
}

DEBILITATION_RULES: Dict[str, Tuple[str, float]] = {
    "Sun": ("Libra", 10.0),
    "Moon": ("Scorpio", 3.0),
    "Mars": ("Cancer", 28.0),
    "Mercury": ("Pisces", 15.0),
    "Jupiter": ("Capricorn", 5.0),
    "Venus": ("Virgo", 27.0),
    "Saturn": ("Aries", 20.0),
    "Rahu": ("Scorpio", 20.0),
    "Ketu": ("Taurus", 20.0),
}

# Moolatrikona: (Sign, min_degree_inclusive, max_degree_inclusive)
MOOLATRIKONA_RULES: Dict[str, Tuple[str, float, float]] = {
    "Sun": ("Leo", 0.0, 20.0),
    "Moon": ("Taurus", 3.0, 30.0),
    "Mars": ("Aries", 0.0, 12.0),
    "Mercury": ("Virgo", 15.0, 20.0),
    "Jupiter": ("Sagittarius", 0.0, 10.0),
    "Venus": ("Libra", 0.0, 15.0),
    "Saturn": ("Aquarius", 0.0, 20.0),
}

OWN_SIGNS: Dict[str, Set[str]] = {
    "Sun": {"Leo"},
    "Moon": {"Cancer"},
    "Mars": {"Aries", "Scorpio"},
    "Mercury": {"Gemini", "Virgo"},
    "Jupiter": {"Sagittarius", "Pisces"},
    "Venus": {"Taurus", "Libra"},
    "Saturn": {"Capricorn", "Aquarius"},
    "Rahu": {"Aquarius"},
    "Ketu": {"Scorpio"},
}

# Combustion (Asta) angular orbs from Sun: (direct_orb_deg, retrograde_orb_deg)
COMBUSTION_ORBS: Dict[str, Tuple[float, float]] = {
    "Moon": (12.0, 12.0),
    "Mars": (17.0, 17.0),
    "Mercury": (14.0, 12.0),
    "Jupiter": (11.0, 11.0),
    "Venus": (10.0, 8.0),
    "Saturn": (15.0, 15.0),
}

# Naisargika Maitri (Natural Relationships per BPHS Ch. 3)
NATURAL_FRIENDS: Dict[str, Set[str]] = {
    "Sun": {"Moon", "Mars", "Jupiter"},
    "Moon": {"Sun", "Mercury"},
    "Mars": {"Sun", "Moon", "Jupiter"},
    "Mercury": {"Sun", "Venus"},
    "Jupiter": {"Sun", "Moon", "Mars"},
    "Venus": {"Mercury", "Saturn", "Rahu", "Ketu"},
    "Saturn": {"Mercury", "Venus", "Rahu", "Ketu"},
    "Rahu": {"Mercury", "Venus", "Saturn"},
    "Ketu": {"Mars", "Venus", "Saturn"},
}

NATURAL_ENEMIES: Dict[str, Set[str]] = {
    "Sun": {"Venus", "Saturn", "Rahu", "Ketu"},
    "Moon": {"Rahu", "Ketu"},
    "Mars": {"Mercury", "Rahu"},
    "Mercury": {"Moon"},
    "Jupiter": {"Mercury", "Venus"},
    "Venus": {"Sun", "Moon"},
    "Saturn": {"Sun", "Moon", "Mars"},
    "Rahu": {"Sun", "Moon", "Mars"},
    "Ketu": {"Sun", "Moon"},
}
