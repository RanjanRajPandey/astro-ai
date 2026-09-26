"""
Coordinate Validation, DMS Formatting, and Gazetteer Lookup.
"""

from dataclasses import dataclass
from typing import Optional, Dict, Tuple


@dataclass(frozen=True)
class CityGazetteerEntry:
    name: str
    state_or_region: str
    country_code: str
    latitude: float
    longitude: float
    timezone_id: str


# Curated high-precision gazetteer for deterministic offline resolution & fast autocomplete
CURATED_GAZETTEER: Dict[str, CityGazetteerEntry] = {
    "new delhi": CityGazetteerEntry("New Delhi", "Delhi", "IN", 28.6139, 77.2090, "Asia/Kolkata"),
    "delhi": CityGazetteerEntry("Delhi", "Delhi", "IN", 28.7041, 77.1025, "Asia/Kolkata"),
    "mumbai": CityGazetteerEntry("Mumbai", "Maharashtra", "IN", 19.0760, 72.8777, "Asia/Kolkata"),
    "bombay": CityGazetteerEntry("Mumbai (Bombay)", "Maharashtra", "IN", 19.0760, 72.8777, "Asia/Kolkata"),
    "bengaluru": CityGazetteerEntry("Bengaluru", "Karnataka", "IN", 12.9716, 77.5946, "Asia/Kolkata"),
    "bangalore": CityGazetteerEntry("Bengaluru (Bangalore)", "Karnataka", "IN", 12.9716, 77.5946, "Asia/Kolkata"),
    "kolkata": CityGazetteerEntry("Kolkata", "West Bengal", "IN", 22.5726, 88.3639, "Asia/Kolkata"),
    "calcutta": CityGazetteerEntry("Kolkata (Calcutta)", "West Bengal", "IN", 22.5726, 88.3639, "Asia/Kolkata"),
    "chennai": CityGazetteerEntry("Chennai", "Tamil Nadu", "IN", 13.0827, 80.2707, "Asia/Kolkata"),
    "madras": CityGazetteerEntry("Chennai (Madras)", "Tamil Nadu", "IN", 13.0827, 80.2707, "Asia/Kolkata"),
    "hyderabad": CityGazetteerEntry("Hyderabad", "Telangana", "IN", 17.3850, 78.4867, "Asia/Kolkata"),
    "pune": CityGazetteerEntry("Pune", "Maharashtra", "IN", 18.5204, 73.8567, "Asia/Kolkata"),
    "ahmedabad": CityGazetteerEntry("Ahmedabad", "Gujarat", "IN", 23.0225, 72.5714, "Asia/Kolkata"),
    "jaipur": CityGazetteerEntry("Jaipur", "Rajasthan", "IN", 26.9124, 75.7873, "Asia/Kolkata"),
    "lucknow": CityGazetteerEntry("Lucknow", "Uttar Pradesh", "IN", 26.8467, 80.9462, "Asia/Kolkata"),
    "varanasi": CityGazetteerEntry("Varanasi", "Uttar Pradesh", "IN", 25.3176, 82.9739, "Asia/Kolkata"),
    "kashi": CityGazetteerEntry("Varanasi (Kashi)", "Uttar Pradesh", "IN", 25.3176, 82.9739, "Asia/Kolkata"),
    "patna": CityGazetteerEntry("Patna", "Bihar", "IN", 25.5941, 85.1376, "Asia/Kolkata"),
    "ranchi": CityGazetteerEntry("Ranchi", "Jharkhand", "IN", 23.3441, 85.3096, "Asia/Kolkata"),
    "bhopal": CityGazetteerEntry("Bhopal", "Madhya Pradesh", "IN", 23.2599, 77.4126, "Asia/Kolkata"),
    "ujjain": CityGazetteerEntry("Ujjain", "Madhya Pradesh", "IN", 23.1765, 75.7885, "Asia/Kolkata"),
    "indore": CityGazetteerEntry("Indore", "Madhya Pradesh", "IN", 22.7196, 75.8577, "Asia/Kolkata"),
    "chandigarh": CityGazetteerEntry("Chandigarh", "Chandigarh", "IN", 30.7333, 76.7794, "Asia/Kolkata"),
    "amritsar": CityGazetteerEntry("Amritsar", "Punjab", "IN", 31.6340, 74.8723, "Asia/Kolkata"),
    "prayagraj": CityGazetteerEntry("Prayagraj", "Uttar Pradesh", "IN", 25.4358, 81.8463, "Asia/Kolkata"),
    "allahabad": CityGazetteerEntry("Prayagraj (Allahabad)", "Uttar Pradesh", "IN", 25.4358, 81.8463, "Asia/Kolkata"),
    "ayodhya": CityGazetteerEntry("Ayodhya", "Uttar Pradesh", "IN", 26.7922, 82.1998, "Asia/Kolkata"),
    "surat": CityGazetteerEntry("Surat", "Gujarat", "IN", 21.1702, 72.8311, "Asia/Kolkata"),
    "kochi": CityGazetteerEntry("Kochi", "Kerala", "IN", 9.9312, 76.2673, "Asia/Kolkata"),
    "thiruvananthapuram": CityGazetteerEntry("Thiruvananthapuram", "Kerala", "IN", 8.5241, 76.9366, "Asia/Kolkata"),
    "guwahati": CityGazetteerEntry("Guwahati", "Assam", "IN", 26.1445, 91.7362, "Asia/Kolkata"),
    "bhubaneswar": CityGazetteerEntry("Bhubaneswar", "Odisha", "IN", 20.2961, 85.8245, "Asia/Kolkata"),
    "dehradun": CityGazetteerEntry("Dehradun", "Uttarakhand", "IN", 30.3165, 78.0322, "Asia/Kolkata"),
    "kathmandu": CityGazetteerEntry("Kathmandu", "Bagmati", "NP", 27.7172, 85.3240, "Asia/Kathmandu"),
    "colombo": CityGazetteerEntry("Colombo", "Western", "LK", 6.9271, 79.8612, "Asia/Colombo"),
    "london": CityGazetteerEntry("London", "England", "GB", 51.5074, -0.1278, "Europe/London"),
    "new york": CityGazetteerEntry("New York", "NY", "US", 40.7128, -74.0060, "America/New_York"),
    "san francisco": CityGazetteerEntry("San Francisco", "CA", "US", 37.7749, -122.4194, "America/Los_Angeles"),
    "los angeles": CityGazetteerEntry("Los Angeles", "CA", "US", 34.0522, -118.2437, "America/Los_Angeles"),
    "chicago": CityGazetteerEntry("Chicago", "IL", "US", 41.8781, -87.6298, "America/Chicago"),
    "toronto": CityGazetteerEntry("Toronto", "Ontario", "CA", 43.6532, -79.3832, "America/Toronto"),
    "sydney": CityGazetteerEntry("Sydney", "NSW", "AU", -33.8688, 151.2093, "Australia/Sydney"),
    "singapore": CityGazetteerEntry("Singapore", "Singapore", "SG", 1.3521, 103.8198, "Asia/Singapore"),
    "dubai": CityGazetteerEntry("Dubai", "Dubai", "AE", 25.2048, 55.2708, "Asia/Dubai"),
    "tokyo": CityGazetteerEntry("Tokyo", "Kanto", "JP", 35.6762, 139.6503, "Asia/Tokyo"),
}


def validate_coordinates(latitude: float, longitude: float) -> Tuple[float, float]:
    """Validate geographic coordinates strictly within [-90..90] and [-180..180]."""
    if latitude is None or longitude is None:
        raise ValueError("Latitude and longitude must not be null.")
    if not (-90.0 <= latitude <= 90.0):
        raise ValueError(f"Latitude {latitude} out of valid range [-90.0, 90.0].")
    if not (-180.0 <= longitude <= 180.0):
        raise ValueError(f"Longitude {longitude} out of valid range [-180.0, 180.0].")
    return round(float(latitude), 6), round(float(longitude), 6)


def decimal_to_dms(deg: float, is_latitude: bool = True) -> str:
    """Convert decimal degrees to Degrees-Minutes-Seconds string with hemisphere."""
    if is_latitude:
        hemi = "N" if deg >= 0 else "S"
    else:
        hemi = "E" if deg >= 0 else "W"
    abs_deg = abs(deg)
    d = int(abs_deg)
    rem_min = (abs_deg - d) * 60.0
    m = int(rem_min)
    s = round((rem_min - m) * 60.0, 1)
    if s >= 60.0:
        s = 0.0
        m += 1
    if m >= 60:
        m = 0
        d += 1
    return f"{d:02d}° {m:02d}' {s:04.1f}\" {hemi}"


def lookup_place_in_gazetteer(place_query: str) -> Optional[CityGazetteerEntry]:
    """Resolve a place name against the curated gazetteer (case-insensitive, supports 'City, State, Country')."""
    if not place_query or not place_query.strip():
        return None
    normalized = place_query.strip().lower()
    if normalized in CURATED_GAZETTEER:
        return CURATED_GAZETTEER[normalized]
    primary_token = normalized.split(",")[0].strip()
    if primary_token in CURATED_GAZETTEER:
        return CURATED_GAZETTEER[primary_token]
    return None
