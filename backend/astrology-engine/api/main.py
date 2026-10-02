import time
from fastapi import FastAPI, Request
from fastapi.responses import PlainTextResponse
from api.aspect_router import router as aspect_router
from api.dasha_router import router as dasha_router
from api.divisional_router import router as divisional_router
from api.evidence_router import router as evidence_router
from api.framework_router import router as framework_router
from api.house_router import router as house_router
from api.location_router import router as location_router
from api.nakshatra_router import router as nakshatra_router
from api.planet_router import router as planet_router
from api.reasoning_router import router as reasoning_router
from api.strength_router import router as strength_router
from api.temporal_router import router as temporal_router
from api.transit_router import router as transit_router
from api.yoga_router import router as yoga_router
from astronomy.ephemeris import get_ephemeris_cache_stats, clear_ephemeris_cache, get_ayanamsha_value
from models.health import EngineHealthResponse
from rules.config import DEFAULT_SPEC

app = FastAPI(
    title="Astro-AI Deterministic Astrology Calculation Engine",
    description=(
        "Isolated deterministic Vedic Astrology (Jyotish) calculation service. "
        "Computes sidereal positions, houses, nakshatras, D1-D60 divisional charts, "
        "5-level Vimshottari Dashas, Drishti, Shadbala, Bhava Bala, Yogas, Transits, "
        "Temporal Analysis, Domain Analysis Frameworks, Evidence Generation, and Reasoning Synthesis."
    ),
    version=DEFAULT_SPEC.specification_version,
)

# Operational Metrics Tracker
START_TIME = time.time()
REQUEST_COUNT = 0


@app.middleware("http")
async def metrics_middleware(request: Request, call_next):
    global REQUEST_COUNT
    REQUEST_COUNT += 1
    start = time.perf_counter()
    response = await call_next(request)
    duration_ms = (time.perf_counter() - start) * 1000.0
    response.headers["X-Response-Time-Ms"] = f"{duration_ms:.2f}"
    return response


app.include_router(location_router)
app.include_router(planet_router)
app.include_router(house_router)
app.include_router(nakshatra_router)
app.include_router(dasha_router)
app.include_router(divisional_router)
app.include_router(aspect_router)
app.include_router(strength_router)
app.include_router(yoga_router)
app.include_router(transit_router)
app.include_router(temporal_router)
app.include_router(framework_router)
app.include_router(evidence_router)
app.include_router(reasoning_router)


@app.get("/", tags=["System"])
def root():
    return {
        "status": "UP",
        "service": "astrology-engine",
        "health": "/health",
        "metrics": "/metrics",
        "specification_version": DEFAULT_SPEC.specification_version,
    }


@app.get("/health", response_model=EngineHealthResponse, tags=["System"])
@app.get("/api/v1/health", response_model=EngineHealthResponse, tags=["System"])
def get_health() -> EngineHealthResponse:
    # Deep health validation: verify Swiss Ephemeris calculates ayanamsha accurately
    test_ayanamsha = get_ayanamsha_value(2451545.0, "LAHIRI")
    if test_ayanamsha <= 0:
        raise RuntimeError("Swiss Ephemeris calculation failed deep health probe.")

    return EngineHealthResponse(
        status="UP",
        service="astrology-engine",
        specification_version=DEFAULT_SPEC.specification_version,
        zodiac_system=DEFAULT_SPEC.zodiac_system,
        ayanamsha=DEFAULT_SPEC.ayanamsha,
        node_type=DEFAULT_SPEC.node_type,
        house_system=DEFAULT_SPEC.house_system,
        dasha_year_days=DEFAULT_SPEC.dasha_year_days,
        rahu_ketu_trinal_aspects=DEFAULT_SPEC.rahu_ketu_trinal_aspects,
    )


@app.get("/metrics", response_class=PlainTextResponse, tags=["System"])
def get_prometheus_metrics():
    """Prometheus exposition format for operational scraping."""
    uptime = time.time() - START_TIME
    stats = get_ephemeris_cache_stats()

    lines = [
        "# HELP astro_engine_uptime_seconds Total runtime of astrology engine in seconds",
        "# TYPE astro_engine_uptime_seconds gauge",
        f"astro_engine_uptime_seconds {uptime:.2f}",
        "# HELP astro_engine_requests_total Total HTTP requests handled",
        "# TYPE astro_engine_requests_total counter",
        f"astro_engine_requests_total {REQUEST_COUNT}",
        "# HELP astro_ephemeris_cache_hits_total Ephemeris LRU cache hits",
        "# TYPE astro_ephemeris_cache_hits_total counter",
        f"astro_ephemeris_cache_hits_total {stats['total_hits']}",
        "# HELP astro_ephemeris_cache_misses_total Ephemeris LRU cache misses",
        "# TYPE astro_ephemeris_cache_misses_total counter",
        f"astro_ephemeris_cache_misses_total {stats['total_misses']}",
        "# HELP astro_ephemeris_cache_current_size Current size of LRU caches",
        "# TYPE astro_ephemeris_cache_current_size gauge",
        f"astro_ephemeris_cache_current_size {stats['current_size']}",
        "# HELP astro_ephemeris_cache_hit_ratio LRU cache hit ratio",
        "# TYPE astro_ephemeris_cache_hit_ratio gauge",
        f"astro_ephemeris_cache_hit_ratio {stats['hit_ratio']:.4f}",
    ]
    return "\n".join(lines) + "\n"


@app.get("/api/v1/admin/cache/stats", tags=["System"])
def get_cache_statistics():
    """Detailed JSON cache diagnostics."""
    return {
        "status": "UP",
        "timestamp": time.time(),
        "ephemeris_cache": get_ephemeris_cache_stats(),
    }


@app.post("/api/v1/admin/cache/clear", tags=["System"])
def clear_caches():
    """Evict all ephemeris caches."""
    clear_ephemeris_cache()
    return {
        "status": "SUCCESS",
        "message": "All ephemeris caches cleared.",
        "timestamp": time.time(),
    }
