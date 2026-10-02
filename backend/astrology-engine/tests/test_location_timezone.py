from datetime import date, time
import pytest
from fastapi.testclient import TestClient
from api.main import app
from coordinates.validator import validate_coordinates, decimal_to_dms, lookup_place_in_gazetteer
from timezone.resolver import resolve_birth_timestamp, resolve_timezone_from_coordinates

client = TestClient(app)


def test_coordinate_validation_and_dms():
    lat, lon = validate_coordinates(28.6139, 77.2090)
    assert lat == 28.6139
    assert lon == 77.2090
    assert decimal_to_dms(28.6139, is_latitude=True) == "28° 36' 50.0\" N"
    assert decimal_to_dms(-33.8688, is_latitude=True) == "33° 52' 07.7\" S"
    assert decimal_to_dms(-74.0060, is_latitude=False) == "74° 00' 21.6\" W"

    with pytest.raises(ValueError):
        validate_coordinates(95.0, 77.0)
    with pytest.raises(ValueError):
        validate_coordinates(28.0, -190.0)


def test_gazetteer_lookup_and_timezone_finder():
    delhi = lookup_place_in_gazetteer("New Delhi, India")
    assert delhi is not None
    assert delhi.country_code == "IN"
    tz = resolve_timezone_from_coordinates(delhi.latitude, delhi.longitude)
    assert tz in ("Asia/Kolkata", "Asia/Calcutta")


def test_standard_ist_birth_timestamp_resolution():
    # 1990-05-15 14:30:00 IST (+05:30) -> 1990-05-15 09:00:00 UTC
    resolved = resolve_birth_timestamp(
        birth_date=date(1990, 5, 15),
        birth_time=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
    )
    assert resolved.timezone_id == "Asia/Kolkata"
    assert resolved.utc_offset_hours == 5.5
    assert resolved.is_dst is False
    assert resolved.utc_datetime_iso == "1990-05-15T09:00:00Z"
    assert resolved.birth_time_accurate is True
    # Verify Julian Day UT for 1990-05-15 09:00 UT (2448026.875)
    assert abs(resolved.julian_day_ut - 2448026.875) < 1e-6


def test_historical_indian_war_time_resolution():
    # 1944-08-20 08:11:00 in Mumbai fell under British Indian War Time (UTC+06:30)
    resolved = resolve_birth_timestamp(
        birth_date=date(1944, 8, 20),
        birth_time=time(8, 11, 0),
        latitude=19.0760,
        longitude=72.8777,
    )
    assert resolved.utc_offset_hours == 6.5
    assert resolved.is_dst is True
    assert "Indian War Time" in (resolved.historical_note or "")
    assert resolved.utc_datetime_iso == "1944-08-20T01:41:00Z"


def test_us_daylight_saving_time_vs_standard_time():
    # New York in July (EDT = UTC-4) vs January (EST = UTC-5)
    summer = resolve_birth_timestamp(
        birth_date=date(2000, 7, 15),
        birth_time=time(10, 0, 0),
        latitude=40.7128,
        longitude=-74.0060,
    )
    assert summer.timezone_id == "America/New_York"
    assert summer.utc_offset_hours == -4.0
    assert summer.is_dst is True
    assert summer.utc_datetime_iso == "2000-07-15T14:00:00Z"

    winter = resolve_birth_timestamp(
        birth_date=date(2000, 1, 15),
        birth_time=time(10, 0, 0),
        latitude=40.7128,
        longitude=-74.0060,
    )
    assert winter.utc_offset_hours == -5.0
    assert winter.is_dst is False
    assert winter.utc_datetime_iso == "2000-01-15T15:00:00Z"


def test_missing_birth_time_handling_via_api():
    response = client.post(
        "/api/v1/location/resolve",
        json={
            "place_of_birth": "Varanasi",
            "date_of_birth": "1995-11-20",
            "time_of_birth": None,
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["birth_time_accurate"] is False
    assert "Ascendant (Lagna)" in data["historical_note"]
    assert data["timezone_id"] == "Asia/Kolkata"
    assert data["utc_datetime_iso"] == "1995-11-20T06:30:00Z"
