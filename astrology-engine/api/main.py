from fastapi import FastAPI
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
