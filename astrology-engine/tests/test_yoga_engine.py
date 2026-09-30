from datetime import date, time
from models.yogas import YogaCalculationRequest
from yogas.detector import detect_all_yogas


def test_yoga_and_dosha_catalog_evaluation() -> None:
    req = YogaCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
    )
    resp = detect_all_yogas(req)

    # Catalog evaluates 24+ classical yogas and doshas
    assert resp.total_evaluated_count >= 24
    assert len(resp.all_evaluated_yogas) == resp.total_evaluated_count
    assert len(resp.active_yogas) >= 1

    # Check that all required fields and audit conditions are populated
    codes = {y.yoga_code for y in resp.all_evaluated_yogas}
    assert "GAJAKESARI_YOGA" in codes
    assert "BUDHADITYA_YOGA" in codes
    assert "DHARMA_KARMADHIPATI_RAJA_YOGA" in codes
    assert "MANGAL_KUJA_DOSHA" in codes
    assert "KALA_SARPA_YOGA" in codes

    for y in resp.all_evaluated_yogas:
        assert len(y.required_conditions) >= 1
        assert len(y.detected_conditions) >= 1
        assert y.status in ("ACTIVE", "CANCELLED_OR_MITIGATED", "NOT_FORMED")
        assert y.strength in ("VERY_STRONG", "STRONG", "MODERATE", "MITIGATED", "INACTIVE")
