from fastapi.testclient import TestClient
from api.main import app
from astronomy.ephemeris import get_ephemeris_cache_stats, clear_ephemeris_cache, get_ayanamsha_value

client = TestClient(app)


def test_metrics_endpoint():
    """Verify Prometheus /metrics returns expected standard format and keys."""
    # Warm up cache with a call
    get_ayanamsha_value(2451545.0, "LAHIRI")
    get_ayanamsha_value(2451545.0, "LAHIRI")

    response = client.get("/metrics")
    assert response.status_code == 200
    assert "text/plain" in response.headers["content-type"]
    text = response.text

    assert "astro_engine_uptime_seconds" in text
    assert "astro_engine_requests_total" in text
    assert "astro_ephemeris_cache_hits_total" in text
    assert "astro_ephemeris_cache_current_size" in text
    assert "astro_ephemeris_cache_hit_ratio" in text


def test_admin_cache_stats_and_clear():
    """Verify admin cache stats and clear endpoints."""
    # Populate cache
    get_ayanamsha_value(2451545.0, "LAHIRI")
    get_ayanamsha_value(2451545.0, "LAHIRI")

    res = client.get("/api/v1/admin/cache/stats")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "UP"
    assert "ephemeris_cache" in data
    assert data["ephemeris_cache"]["total_hits"] >= 1

    # Clear cache
    clear_res = client.post("/api/v1/admin/cache/clear")
    assert clear_res.status_code == 200
    clear_data = clear_res.json()
    assert clear_data["status"] == "SUCCESS"

    # Verify cleared stats
    cleared_stats = get_ephemeris_cache_stats()
    assert cleared_stats["current_size"] == 0


def test_deep_health_check():
    """Verify /health executes deep Swiss Ephemeris probe."""
    res = client.get("/health")
    assert res.status_code == 200
    body = res.json()
    assert body["status"] == "UP"
    assert body["ayanamsha"] == "LAHIRI"
