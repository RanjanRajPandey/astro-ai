from fastapi.testclient import TestClient
from api.main import app

client = TestClient(app)


def test_engine_health_and_locked_specification():
    response = client.get("/health")
    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "UP"
    assert payload["service"] == "astrology-engine"
    assert payload["zodiac_system"] == "SIDEREAL"
    assert payload["ayanamsha"] == "LAHIRI"
    assert payload["node_type"] == "MEAN_NODE"
    assert payload["house_system"] == "WHOLE_SIGN_WITH_SRIPATI"
    assert abs(payload["dasha_year_days"] - 365.2425) < 1e-6
    assert payload["rahu_ketu_trinal_aspects"] is False
