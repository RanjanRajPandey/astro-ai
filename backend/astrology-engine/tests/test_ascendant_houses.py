from datetime import date, time
from fastapi.testclient import TestClient
from api.main import app
from ascendant.calculator import compute_arudha_pada_sign_index
from houses.calculator import calculate_houses_and_ascendant
from timezone.resolver import resolve_birth_timestamp

client = TestClient(app)


def test_arudha_pada_classical_swastika_exceptions():
    # Case 1: Lord in 1st from house (e.g., Aries=0, Mars in Aries=0) -> raw pada is 0 (1st from house)
    # Classical BPHS exception: 10th from raw pada -> (0 + 9) % 12 = 9 (Capricorn, 10th sign)
    assert compute_arudha_pada_sign_index(0, 0) == 9

    # Case 2: Lord in 4th from house (e.g., Aries=0, Mars in Cancer=3) -> dist=3 -> raw pada=(3+3)%12 = 6 (Libra, 7th from house)
    # Classical BPHS exception: 10th from raw pada -> (6 + 9) % 12 = 3 (Cancer, 4th from house)
    assert compute_arudha_pada_sign_index(0, 3) == 3

    # Case 3: Standard case without exception: Lord in 2nd from house (Aries=0, Mars in Taurus=1) -> dist=1 -> raw pada=2 (Gemini, 3rd house)
    assert compute_arudha_pada_sign_index(0, 1) == 2


def test_houses_and_ascendant_calculation_golden_reference():
    resolved = resolve_birth_timestamp(
        birth_date=date(1990, 5, 15),
        birth_time=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
    )
    result = calculate_houses_and_ascendant(
        jd_ut=resolved.julian_day_ut,
        latitude=28.6139,
        longitude=77.2090,
    )

    assert result.ascendant.sign == "Virgo"
    assert result.ascendant.sanskrit_sign == "Kanya"
    assert result.ascendant.lagna_lord == "Mercury"
    assert len(result.houses) == 12

    # Verify all 9 Grahas are accounted for across the 12 Whole-Sign houses AND 12 Sripati Chalit houses
    total_occupants = sum(len(h.occupants) for h in result.houses)
    total_chalit_occupants = sum(len(h.chalit_occupants) for h in result.houses)
    assert total_occupants == 9
    assert total_chalit_occupants == 9

    # House 1 is Virgo (Mercury), House 9 is Taurus (Venus, occupied by Sun)
    h1 = result.houses[0]
    assert h1.house_number == 1
    assert h1.sign == "Virgo"
    assert h1.lord_planet == "Mercury"
    assert "KENDRA" in h1.classifications
    assert "TRIKONA" in h1.classifications

    h9 = result.houses[8]
    assert h9.house_number == 9
    assert h9.sign == "Taurus"
    assert h9.lord_planet == "Venus"
    assert "Sun" in h9.occupants

    # House 5 is Capricorn (occupied by Saturn in Own Sign)
    h5 = result.houses[4]
    assert h5.house_number == 5
    assert h5.sign == "Capricorn"
    assert h5.lord_planet == "Saturn"
    assert "Saturn" in h5.occupants
    assert h5.lord_dignity == "OWN_SIGN"


def test_houses_fastapi_endpoint():
    response = client.post(
        "/api/v1/houses/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert data["house_system"] == "WHOLE_SIGN_WITH_SRIPATI"
    assert data["ascendant"]["sign"] == "Virgo"
    assert len(data["houses"]) == 12
    for h in data["houses"]:
        assert 10.0 <= h["baseline_strength_score"] <= 100.0
        assert h["strength_grade"] in ("STRONG", "MODERATE", "CHALLENGED")
