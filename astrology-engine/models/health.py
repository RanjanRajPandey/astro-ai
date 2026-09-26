from pydantic import BaseModel


class EngineHealthResponse(BaseModel):
    status: str
    service: str
    specification_version: str
    zodiac_system: str
    ayanamsha: str
    node_type: str
    house_system: str
    dasha_year_days: float
    rahu_ketu_trinal_aspects: bool
