from datetime import date, time
from fastapi.testclient import TestClient

from api.main import app
from divisional.shodashavarga import (
    SHODASHAVARGA_METADATA,
    calculate_shodashavarga,
    compute_varga_sign_and_part,
)
from models.divisional import DivisionalCalculationRequest

client = TestClient(app)


def test_bphs_shodashavarga_formulas_across_all_16_divisions():
    assert len(SHODASHAVARGA_METADATA) == 16

    # 1. D9 Navamsha: 0°01' Aries (Fire, 1st pada) -> Aries (0), Vargottama!
    s_d9, p_d9 = compute_varga_sign_and_part(0.5, 9)
    assert s_d9 == 0 and p_d9 == 1

    # 2. D2 Hora: Odd sign Aries (0..15 -> Leo=4, 15..30 -> Cancer=3)
    assert compute_varga_sign_and_part(5.0, 2)[0] == 4
    assert compute_varga_sign_and_part(20.0, 2)[0] == 3
    # Even sign Taurus (30..45 -> Cancer=3, 45..60 -> Leo=4)
    assert compute_varga_sign_and_part(35.0, 2)[0] == 3
    assert compute_varga_sign_and_part(50.0, 2)[0] == 4

    # 3. D3 Drekkana: Aries 0..10->Aries(0), 10..20->Leo(4), 20..30->Sagittarius(8)
    assert compute_varga_sign_and_part(2.0, 3)[0] == 0
    assert compute_varga_sign_and_part(15.0, 3)[0] == 4
    assert compute_varga_sign_and_part(25.0, 3)[0] == 8

    # 4. D30 Trimshamsha: Odd sign Aries (0..5->Aries=0, 5..10->Aquarius=10, 10..18->Sag=8, 18..25->Gem=2, 25..30->Libra=6)
    assert compute_varga_sign_and_part(3.0, 30)[0] == 0
    assert compute_varga_sign_and_part(7.0, 30)[0] == 10
    assert compute_varga_sign_and_part(14.0, 30)[0] == 8
    assert compute_varga_sign_and_part(21.0, 30)[0] == 2
    assert compute_varga_sign_and_part(28.0, 30)[0] == 6


def test_full_shodashavarga_calculation_and_endpoint():
    req = DivisionalCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
    )
    res = calculate_shodashavarga(req)
    assert len(res.charts) == 16

    expected_codes = [
        "D1", "D2", "D3", "D4", "D7", "D9", "D10", "D12",
        "D16", "D20", "D24", "D27", "D30", "D40", "D45", "D60",
    ]
    assert [c.varga_code for c in res.charts] == expected_codes

    for chart in res.charts:
        assert len(chart.planets) == 9
        assert len(chart.houses) == 12
        # Verify Rahu and Ketu are present
        planet_names = [p.planet for p in chart.planets]
        assert "Sun" in planet_names and "Rahu" in planet_names and "Ketu" in planet_names
        if chart.varga_code == "D60":
            for p in chart.planets:
                assert p.shashtiamsha_name is not None
                assert p.shashtiamsha_quality in ("BENEFIC", "MALEFIC")

    # FastAPI endpoint test
    http_res = client.post(
        "/api/v1/divisional/calculate",
        json={
            "date_of_birth": "1990-05-15",
            "time_of_birth": "14:30:00",
            "latitude": 28.6139,
            "longitude": 77.2090,
            "timezone_id": "Asia/Kolkata",
        },
    )
    assert http_res.status_code == 200
    body = http_res.json()
    assert len(body["charts"]) == 16
