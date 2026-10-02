"""
Historical Timezone, UTC Timestamp, and Julian Day (JD_UT / JD_ET) Resolution Engine.
"""

from dataclasses import dataclass
from datetime import date, time, datetime, timedelta, timezone
from typing import Optional
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError
import swisseph as swe
from timezonefinder import TimezoneFinder
from coordinates.validator import validate_coordinates

_TF_INSTANCE: Optional[TimezoneFinder] = None


def _get_timezone_finder() -> TimezoneFinder:
    global _TF_INSTANCE
    if _TF_INSTANCE is None:
        _TF_INSTANCE = TimezoneFinder(in_memory=True)
    return _TF_INSTANCE


@dataclass(frozen=True)
class ResolvedBirthTime:
    timezone_id: str
    utc_offset_hours: float
    is_dst: bool
    historical_note: Optional[str]
    local_datetime_iso: str
    utc_datetime_iso: str
    julian_day_ut: float
    delta_t_seconds: float
    julian_day_et: float
    birth_time_accurate: bool


def resolve_timezone_from_coordinates(latitude: float, longitude: float) -> str:
    """Determine canonical IANA timezone ID from latitude and longitude."""
    lat, lon = validate_coordinates(latitude, longitude)
    tf = _get_timezone_finder()
    tz_name = tf.timezone_at(lng=lon, lat=lat)
    if not tz_name:
        tz_name = tf.closest_timezone_at(lng=lon, lat=lat)
    if not tz_name:
        raise ValueError(f"Could not resolve IANA timezone for coordinates ({lat}, {lon}).")
    return tz_name


def _check_indian_historical_timezone_override(
    tz_id: str, local_dt: datetime, longitude: float
) -> Optional[tuple[float, bool, str]]:
    """
    Explicitly verify historical Indian timezone rules where standard tzdata builds
    occasionally vary across platforms:
    1. Indian War Time (1942-09-01 00:00 to 1945-10-15 00:00): UTC+06:30 (+1h DST over IST).
    2. Pre-IST Standardization (before 1906-01-01): Local Mean Time (longitude * 4 min).
    """
    if tz_id not in ("Asia/Kolkata", "Asia/Calcutta"):
        return None

    war_time_start = datetime(1942, 9, 1, 0, 0, 0)
    war_time_end = datetime(1945, 10, 15, 0, 0, 0)
    if war_time_start <= local_dt < war_time_end:
        return (
            6.5,
            True,
            "Historical Indian War Time (1942-09-01 to 1945-10-15): UTC+06:30 applied.",
        )

    ist_adoption = datetime(1906, 1, 1, 0, 0, 0)
    if local_dt < ist_adoption:
        lmt_hours = round(longitude / 15.0, 4)
        return (
            lmt_hours,
            False,
            f"Pre-1906 Indian Standard Time adoption: Local Mean Time (UTC+{lmt_hours:.4f}h) applied.",
        )

    return None


def resolve_birth_timestamp(
    birth_date: date,
    birth_time: Optional[time],
    latitude: float,
    longitude: float,
    explicit_timezone_id: Optional[str] = None,
) -> ResolvedBirthTime:
    """
    Resolve exact UTC birth timestamp and Swiss Ephemeris Julian Day (JD_UT and JD_ET)
    from birth date, birth time, and coordinates.
    """
    lat, lon = validate_coordinates(latitude, longitude)
    tz_id = explicit_timezone_id or resolve_timezone_from_coordinates(lat, lon)

    try:
        zone = ZoneInfo(tz_id)
    except ZoneInfoNotFoundError as exc:
        raise ValueError(f"Invalid or unsupported IANA timezone: '{tz_id}'.") from exc

    birth_time_accurate = birth_time is not None
    effective_time = birth_time if birth_time is not None else time(12, 0, 0)

    naive_local = datetime.combine(birth_date, effective_time)
    historical_override = _check_indian_historical_timezone_override(tz_id, naive_local, lon)

    if historical_override is not None:
        offset_hours, is_dst, note = historical_override
        tz_offset = timezone(timedelta(hours=offset_hours))
        aware_local = naive_local.replace(tzinfo=tz_offset)
        utc_dt = aware_local.astimezone(timezone.utc)
    else:
        aware_local = naive_local.replace(tzinfo=zone)
        offset_td = aware_local.utcoffset() or timedelta(0)
        dst_td = aware_local.dst() or timedelta(0)
        offset_hours = round(offset_td.total_seconds() / 3600.0, 4)
        is_dst = dst_td.total_seconds() != 0
        note = "Daylight Saving Time (DST) active at birth." if is_dst else None
        utc_dt = aware_local.astimezone(timezone.utc)

    if not birth_time_accurate:
        missing_note = (
            "Birth time was not provided; defaulted to 12:00:00 Noon (Surya/Chandra baseline). "
            "Ascendant (Lagna), House Cusps, D9/D60 Divisional Charts, and fine Dasha dates may vary."
        )
        note = f"{note} {missing_note}".strip() if note else missing_note

    # Compute Julian Day (UT) and Ephemeris Time (ET) using Swiss Ephemeris
    ut_hour_decimal = (
        utc_dt.hour
        + (utc_dt.minute / 60.0)
        + ((utc_dt.second + utc_dt.microsecond / 1e6) / 3600.0)
    )
    jd_ut = swe.julday(utc_dt.year, utc_dt.month, utc_dt.day, ut_hour_decimal, swe.GREG_CAL)
    delta_t_days = swe.deltat(jd_ut)
    delta_t_seconds = round(delta_t_days * 86400.0, 4)
    jd_et = jd_ut + delta_t_days

    return ResolvedBirthTime(
        timezone_id=tz_id,
        utc_offset_hours=offset_hours,
        is_dst=is_dst,
        historical_note=note,
        local_datetime_iso=aware_local.isoformat(),
        utc_datetime_iso=utc_dt.isoformat().replace("+00:00", "Z"),
        julian_day_ut=round(jd_ut, 8),
        delta_t_seconds=delta_t_seconds,
        julian_day_et=round(jd_et, 8),
        birth_time_accurate=birth_time_accurate,
    )
