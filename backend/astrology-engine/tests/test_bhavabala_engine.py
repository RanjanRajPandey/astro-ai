from datetime import date, time
from models.strength import BhavaBalaCalculationRequest
from strength.bhavabala import calculate_bhava_bala, compute_bhava_dig_bala


def test_bhava_dig_bala_sign_natures() -> None:
    # Nara/Human signs (Gemini, Virgo, Libra, Aquarius) peak in H1 (60V) and are 0 in H7
    nature_1, val_1 = compute_bhava_dig_bala(1, "Gemini")
    assert nature_1 == "NARA_BIPED"
    assert val_1 == 60.0
    _, val_7 = compute_bhava_dig_bala(7, "Gemini")
    assert val_7 == 0.0

    # Jalachara/Water signs (Cancer, Pisces) peak in H4 (60V) and are 0 in H10
    nature_4, val_4 = compute_bhava_dig_bala(4, "Cancer")
    assert nature_4 == "JALACHARA_WATER"
    assert val_4 == 60.0

    # Keeta/Insect sign (Scorpio) peaks in H7 (60V) and is 0 in H1
    nature_7, val_sc_7 = compute_bhava_dig_bala(7, "Scorpio")
    assert nature_7 == "KEETA_INSECT"
    assert val_sc_7 == 60.0

    # Chatushpada/Quadruped signs (Aries, Taurus, Leo, Sagittarius, Capricorn) peak in H10 (60V)
    nature_10, val_10 = compute_bhava_dig_bala(10, "Leo")
    assert nature_10 == "CHATUSHPADA_QUADRUPED"
    assert val_10 == 60.0


def test_full_bhava_bala_12_houses_and_purusharthas() -> None:
    req = BhavaBalaCalculationRequest(
        date_of_birth=date(1990, 5, 15),
        time_of_birth=time(14, 30, 0),
        latitude=28.6139,
        longitude=77.2090,
        timezone_id="Asia/Kolkata",
        ayanamsha_type="LAHIRI",
    )
    resp = calculate_bhava_bala(req)

    assert len(resp.houses) == 12
    assert len(resp.purushartha_summaries) == 4
    ranks = sorted(h.rank for h in resp.houses)
    assert ranks == list(range(1, 13))

    for h in resp.houses:
        assert h.bhavadhipati_bala > 0.0
        assert 0.0 <= h.bhava_dig_bala <= 60.0
        assert h.total_bhava_bala_virupas >= 60.0
        assert round(h.total_bhava_bala_virupas / 60.0, 3) == h.total_bhava_bala_rupas
        assert h.strength_grade in ("VERY_STRONG", "STRONG", "MODERATE", "WEAK")
