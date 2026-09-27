from fastapi import FastAPI
from api.location_router import router as location_router
from api.planet_router import router as planet_router
from models.health import EngineHealthResponse
from rules.config import DEFAULT_SPEC

app = FastAPI(
    title="Astro-AI Deterministic Astrology Calculation Engine",
    description=(
        "Isolated deterministic Vedic Astrology (Jyotish) calculation service. "
        "Computes sidereal positions, houses, nakshatras, D1-D60 divisional charts, "
        "5-level Vimshottari Dashas, Drishti, Shadbala, Bhava Bala, Yogas, and Transits."
    ),
    version=DEFAULT_SPEC.specification_version,
)

app.include_router(location_router)
app.include_router(planet_router)


@app.get("/health", response_model=EngineHealthResponse, tags=["System"])
def get_health() -> EngineHealthResponse:
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
