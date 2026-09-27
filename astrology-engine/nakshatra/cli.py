"""
CLI Bridge for invoking the deterministic Python Nakshatra Engine
directly with JSON input/output when HTTP daemon is not running.
"""

import json
import sys
from dataclasses import asdict
from datetime import date, time
from nakshatra.calculator import calculate_nakshatras
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
    res = calculate_nakshatras(
        jd_ut=resolved_time.julian_day_ut,
        latitude=lat,
        longitude=lon,
        ayanamsha_type=ayanamsha,
        node_type=node_type,
    )
    output = {
        "utc_datetime_iso": resolved_time.utc_datetime_iso,
        "julian_day_ut": res.julian_day_ut,
        "ayanamsha_type": res.ayanamsha_type,
        "ayanamsha_value": res.ayanamsha_value,
        "janma_nakshatra": res.janma_nakshatra,
        "janma_nakshatra_index": res.janma_nakshatra_index,
        "janma_pada": res.janma_pada,
        "janma_nakshatra_lord": res.janma_nakshatra_lord,
        "janma_rashi": res.janma_rashi,
        "moon_elapsed_fraction": res.moon_elapsed_fraction,
        "moon_remaining_fraction": res.moon_remaining_fraction,
        "placements": [asdict(pl) for pl in res.placements],
    }
    sys.stdout.write(json.dumps(output))


if __name__ == "__main__":
    main()
