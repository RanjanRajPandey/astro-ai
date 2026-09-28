from datetime import date, time
from models.strength import ShadbalaCalculationRequest
from strength.shadbala import (
    calculate_shadbala_and_vimshopaka,
    compute_dig_bala,
    compute_uchcha_bala,
)


def test_uchcha_bala_peaks_and_troughs() -> None:
    # Sun deep exaltation is Aries 10° (10.0°) -> 60 Virupas
    assert compute_uchcha_bala("Sun", 10.0) == 60.0
    # Sun deep debilitation is Libra 10° (190.0°) -> 0 Virupas
    assert compute_uchcha_bala("Sun", 190.0) == 0.0
    # Jupiter deep exaltation is Cancer 5° (95.0°) -> 60 Virupas
    assert compute_uchcha_bala("Jupiter", 95.0) == 60.0


def test_dig_bala_directional_peaks() -> None:
    # Jupiter has full Dig Bala (60 Virupas) at Ascendant (1st cusp) and 0 at 7th cusp
    assert compute_dig_bala("Jupiter", 45.0, 45.0) == 60.0
    assert compute_dig_bala("Jupiter", 225.0, 45.0) == 0.0
    # Sun & Mars have full Dig Bala at 10th cusp (asc + 270) and 0 at 4th cusp (asc + 90)
    assert compute_dig_bala("Sun", 315.0, 45.0) == 60.0
    assert compute_dig_bala("Mars", 135.0, 45.0) == 0.0
    # Saturn has full Dig Bala at 7th cusp (asc + 180)
    assert compute_dig_bala("Saturn", 225.0, 45.0) == 60.0


def test_full_shadbala_and_vimshopaka_calculation() -> None:
    req = ShadbalaCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
    )
    resp = calculate_shadbala_and_vimshopaka(req)

    assert len(resp.planets) == 9
    ranks = sorted(p.rank for p in resp.planets)
    assert ranks == [1, 2, 3, 4, 5, 6, 7, 8, 9]

    for p in resp.planets:
        assert p.sthana_bala > 0.0
        assert 0.0 <= p.dig_bala <= 60.0
        assert p.kala_bala > 0.0
        assert 0.0 <= p.chesta_bala <= 60.0
        assert p.naisargika_bala > 0.0
        assert p.total_shadbala_virupas >= 60.0
        assert round(p.total_shadbala_virupas / 60.0, 3) == p.total_shadbala_rupas
        assert 0.0 < p.vimshopaka_bala <= 20.0
        assert p.strength_grade in ("VERY_STRONG", "ADEQUATE", "MODERATE", "WEAK")
