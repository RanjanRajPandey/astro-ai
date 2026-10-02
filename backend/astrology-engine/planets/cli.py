"""
CLI Bridge for invoking the deterministic Python planetary calculation engine
directly with JSON input/output when HTTP daemon is not running (e.g., standalone JUnit tests).
"""

import json
import sys
from dataclasses import asdict
from datetime import date, time
from planets.calculator import calculate_planetary_positions
from timezone.resolver import resolve_birth_timestamp


def main() -> None:
    payload = json.loads(sys.stdin.read())
    dob = date.fromisoformat(payload["date_of_birth"])
    tob_str = payload.get("time_of_birth")
    tob = time.fromisoformat(tob_str) if tob_str else None
    lat = float(payload["latitude"])
    lon = float(payload["longitude"])
    tz_id = payload.get("timezone_id")
    ayanamsha = payload.get("ayanamsha_type", "LAHIRI")
    node_type = payload.get("node_type", "MEAN_NODE")

    resolved_time = resolve_birth_timestamp(
        birth_date=dob,
        birth_time=tob,
        latitude=lat,
        longitude=lon,
        explicit_timezone_id=tz_id,
    )
    calc = calculate_planetary_positions(
        jd_ut=resolved_time.julian_day_ut,
        latitude=lat,
        longitude=lon,
        ayanamsha_type=ayanamsha,
        node_type=node_type,
    )
    output = {
        "utc_datetime_iso": resolved_time.utc_datetime_iso,
        "julian_day_ut": calc.julian_day_ut,
        "ayanamsha_type": calc.ayanamsha_type,
        "ayanamsha_value": calc.ayanamsha_value,
        "node_type": calc.node_type,
        "ascendant_longitude": calc.ascendant_longitude,
        "ascendant_sign": calc.ascendant_sign,
        "ascendant_sign_index": calc.ascendant_sign_index,
        "ascendant_degree_in_sign": calc.ascendant_degree_in_sign,
        "ascendant_dms": calc.ascendant_dms,
        "planets": [asdict(p) for p in calc.planets],
    }
    sys.stdout.write(json.dumps(output))


if __name__ == "__main__":
    main()
