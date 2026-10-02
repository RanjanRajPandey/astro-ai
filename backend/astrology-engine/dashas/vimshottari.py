from datetime import datetime, timedelta, timezone
from typing import List, Optional, Tuple

from models.dashas import (
    ActiveDashaStackItem,
    DashaCalculationRequest,
    DashaCalculationResponse,
    DashaPeriodNode,
)
from planets.calculator import calculate_planetary_positions
from timezone.resolver import resolve_birth_timestamp

DAYS_PER_YEAR: float = 365.2425
SECONDS_PER_DAY: float = 86400.0
SECONDS_PER_YEAR: float = DAYS_PER_YEAR * SECONDS_PER_DAY
VIMSHOTTARI_TOTAL_YEARS: float = 120.0
NAKSHATRA_SPAN_DEGREES: float = 360.0 / 27.0  # 13°20' = 13.333333333333334°

VIMSHOTTARI_SEQUENCE: List[str] = [
    "Ketu",
    "Venus",
    "Sun",
    "Moon",
    "Mars",
    "Rahu",
    "Jupiter",
    "Saturn",
    "Mercury",
]

VIMSHOTTARI_YEARS = {
    "Ketu": 7.0,
    "Venus": 20.0,
    "Sun": 6.0,
    "Moon": 10.0,
    "Mars": 7.0,
    "Rahu": 18.0,
    "Jupiter": 16.0,
    "Saturn": 19.0,
    "Mercury": 17.0,
}

LEVEL_NAMES = {
    1: "MAHADASHA",
    2: "ANTARDASHA",
    3: "PRATYANTARDASHA",
    4: "SOOKSHMA_DASHA",
    5: "PRANA_DASHA",
}


def _to_iso_utc(dt: datetime) -> str:
    return dt.astimezone(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def _parse_iso_utc(iso_str: str) -> datetime:
    cleaned = iso_str.strip()
    if cleaned.endswith("Z"):
        cleaned = cleaned[:-1] + "+00:00"
    dt = datetime.fromisoformat(cleaned)
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc)


def _rotated_sequence(start_planet: str) -> List[str]:
    idx = VIMSHOTTARI_SEQUENCE.index(start_planet)
    return VIMSHOTTARI_SEQUENCE[idx:] + VIMSHOTTARI_SEQUENCE[:idx]


def _format_balance_ymd(balance_years: float) -> str:
    whole_years = int(balance_years)
    rem_months_float = (balance_years - whole_years) * 12.0
    whole_months = int(rem_months_float)
    rem_days = int(round((rem_months_float - whole_months) * 30.436875))
    if rem_days >= 30:
        whole_months += 1
        rem_days -= 30
    if whole_months >= 12:
        whole_years += 1
        whole_months -= 12
    return f"{whole_years}y {whole_months}m {rem_days}d"


def subdivide_dasha_period(
    parent_planet: str,
    parent_unclamped_start: datetime,
    parent_end: datetime,
    child_level: int,
    birth_utc: datetime,
    target_utc: datetime,
    max_depth: int,
) -> List[DashaPeriodNode]:
    """
    Recursively subdivides a parent Dasha period [parent_unclamped_start, parent_end]
    into its 9 proportional Vimshottari sub-periods starting with parent_planet.
    """
    total_unclamped_seconds = (parent_end - parent_unclamped_start).total_seconds()
    planet_order = _rotated_sequence(parent_planet)
    children: List[DashaPeriodNode] = []

    cursor = parent_unclamped_start
    for idx, child_planet in enumerate(planet_order):
        proportion = VIMSHOTTARI_YEARS[child_planet] / VIMSHOTTARI_TOTAL_YEARS
        child_seconds = total_unclamped_seconds * proportion
        unclamped_start = cursor
        if idx == len(planet_order) - 1:
            child_end = parent_end
        else:
            child_end = unclamped_start + timedelta(seconds=child_seconds)
        cursor = child_end

        # Skip sub-periods that completely elapsed before the moment of birth
        if child_end <= birth_utc:
            continue

        is_birth_balance = unclamped_start < birth_utc
        clamped_start = birth_utc if is_birth_balance else unclamped_start
        effective_days = (child_end - clamped_start).total_seconds() / SECONDS_PER_DAY
        effective_years = effective_days / DAYS_PER_YEAR
        is_active = clamped_start <= target_utc < child_end

        sub_nodes: List[DashaPeriodNode] = []
        if child_level < max_depth:
            sub_nodes = subdivide_dasha_period(
                parent_planet=child_planet,
                parent_unclamped_start=unclamped_start,
                parent_end=child_end,
                child_level=child_level + 1,
                birth_utc=birth_utc,
                target_utc=target_utc,
                max_depth=max_depth,
            )

        children.append(
            DashaPeriodNode(
                planet=child_planet,
                level=child_level,
                level_name=LEVEL_NAMES[child_level],
                start_date_time=_to_iso_utc(clamped_start),
                end_date_time=_to_iso_utc(child_end),
                unclamped_start_date_time=_to_iso_utc(unclamped_start),
                duration_days=round(effective_days, 4),
                duration_years=round(effective_years, 6),
                is_currently_active=is_active,
                is_birth_balance_period=is_birth_balance,
                sub_periods=sub_nodes,
            )
        )

    return children


def _compute_single_level_children(
    parent_node: DashaPeriodNode,
    child_level: int,
    birth_utc: datetime,
    target_utc: datetime,
) -> List[DashaPeriodNode]:
    return subdivide_dasha_period(
        parent_planet=parent_node.planet,
        parent_unclamped_start=_parse_iso_utc(parent_node.unclamped_start_date_time),
        parent_end=_parse_iso_utc(parent_node.end_date_time),
        child_level=child_level,
        birth_utc=birth_utc,
        target_utc=target_utc,
        max_depth=child_level,
    )


def _to_stack_item(node: DashaPeriodNode, target_utc: datetime) -> ActiveDashaStackItem:
    start_dt = _parse_iso_utc(node.start_date_time)
    end_dt = _parse_iso_utc(node.end_date_time)
    total_sec = max(1.0, (end_dt - start_dt).total_seconds())
    elapsed_sec = max(0.0, min(total_sec, (target_utc - start_dt).total_seconds()))
    pct = round((elapsed_sec / total_sec) * 100.0, 2)
    return ActiveDashaStackItem(
        level=node.level,
        level_name=node.level_name,
        planet=node.planet,
        start_date_time=node.start_date_time,
        end_date_time=node.end_date_time,
        unclamped_start_date_time=node.unclamped_start_date_time,
        duration_days=node.duration_days,
        elapsed_percentage=pct,
    )


def calculate_vimshottari_dasha(request: DashaCalculationRequest) -> DashaCalculationResponse:
    """
    Computes the 5-level Vimshottari Dasha timeline (Mahadasha -> Antardasha -> Pratyantardasha
    -> Sookshma Dasha -> Prana Dasha) from the native's exact sidereal Moon longitude.
    """
    resolved_time = resolve_birth_timestamp(
        birth_date=request.date_of_birth,
        birth_time=request.time_of_birth,
        latitude=request.latitude,
        longitude=request.longitude,
        explicit_timezone_id=request.timezone_id,
    )

    planet_res = calculate_planetary_positions(
        jd_ut=resolved_time.julian_day_ut,
        latitude=request.latitude,
        longitude=request.longitude,
        ayanamsha_type=request.ayanamsha_type,
        node_type=request.node_type,
    )

    birth_utc = _parse_iso_utc(resolved_time.utc_datetime_iso)
    if request.target_datetime_iso:
        target_utc = _parse_iso_utc(request.target_datetime_iso)
    else:
        target_utc = datetime.now(timezone.utc)

    moon_pos = next(p for p in planet_res.planets if p.planet == "Moon")
    moon_lon = float(moon_pos.longitude)

    nak_index_0 = int(moon_lon // NAKSHATRA_SPAN_DEGREES) % 27
    deg_in_nak = moon_lon - (nak_index_0 * NAKSHATRA_SPAN_DEGREES)
    elapsed_fraction = max(0.0, min(1.0, deg_in_nak / NAKSHATRA_SPAN_DEGREES))
    remaining_fraction = 1.0 - elapsed_fraction

    birth_dasha_lord = VIMSHOTTARI_SEQUENCE[nak_index_0 % 9]
    maha_order = _rotated_sequence(birth_dasha_lord)

    full_first_maha_years = VIMSHOTTARI_YEARS[birth_dasha_lord]
    birth_balance_years = full_first_maha_years * remaining_fraction
    birth_balance_days = birth_balance_years * DAYS_PER_YEAR

    # If target_utc is before birth_utc, clamp effective target to birth_utc so active_stack is always populated
    effective_target_utc = max(birth_utc, target_utc)

    mahadashas: List[DashaPeriodNode] = []
    elapsed_first_seconds = full_first_maha_years * elapsed_fraction * SECONDS_PER_YEAR
    first_unclamped_start = birth_utc - timedelta(seconds=elapsed_first_seconds)

    cursor = first_unclamped_start
    for idx, maha_planet in enumerate(maha_order):
        maha_years = VIMSHOTTARI_YEARS[maha_planet]
        maha_seconds = maha_years * SECONDS_PER_YEAR
        unclamped_start = cursor
        maha_end = unclamped_start + timedelta(seconds=maha_seconds)
        cursor = maha_end

        is_birth_balance = idx == 0
        clamped_start = birth_utc if is_birth_balance else unclamped_start
        effective_days = (maha_end - clamped_start).total_seconds() / SECONDS_PER_DAY
        effective_years = effective_days / DAYS_PER_YEAR
        is_active = clamped_start <= effective_target_utc < maha_end

        # Build Level 2 (Antardasha) and Level 3 (Pratyantardasha) inside each Mahadasha
        antardashas = subdivide_dasha_period(
            parent_planet=maha_planet,
            parent_unclamped_start=unclamped_start,
            parent_end=maha_end,
            child_level=2,
            birth_utc=birth_utc,
            target_utc=effective_target_utc,
            max_depth=3,
        )

        mahadashas.append(
            DashaPeriodNode(
                planet=maha_planet,
                level=1,
                level_name=LEVEL_NAMES[1],
                start_date_time=_to_iso_utc(clamped_start),
                end_date_time=_to_iso_utc(maha_end),
                unclamped_start_date_time=_to_iso_utc(unclamped_start),
                duration_days=round(effective_days, 4),
                duration_years=round(effective_years, 6),
                is_currently_active=is_active,
                is_birth_balance_period=is_birth_balance,
                sub_periods=antardashas,
            )
        )

    # Resolve Active 5-Level Dasha Stack (L1 -> L2 -> L3 -> L4 -> L5)
    active_maha = next((m for m in mahadashas if m.is_currently_active), mahadashas[-1])
    active_antar = next(
        (a for a in active_maha.sub_periods if a.is_currently_active),
        active_maha.sub_periods[-1],
    )
    active_pratyantar = next(
        (p for p in active_antar.sub_periods if p.is_currently_active),
        active_antar.sub_periods[-1],
    )

    active_sookshma_list = _compute_single_level_children(
        parent_node=active_pratyantar,
        child_level=4,
        birth_utc=birth_utc,
        target_utc=effective_target_utc,
    )
    active_sookshma = next(
        (s for s in active_sookshma_list if s.is_currently_active),
        active_sookshma_list[-1],
    )

    active_prana_list = _compute_single_level_children(
        parent_node=active_sookshma,
        child_level=5,
        birth_utc=birth_utc,
        target_utc=effective_target_utc,
    )
    active_prana = next(
        (pr for pr in active_prana_list if pr.is_currently_active),
        active_prana_list[-1],
    )

    active_stack = [
        _to_stack_item(active_maha, effective_target_utc),
        _to_stack_item(active_antar, effective_target_utc),
        _to_stack_item(active_pratyantar, effective_target_utc),
        _to_stack_item(active_sookshma, effective_target_utc),
        _to_stack_item(active_prana, effective_target_utc),
    ]

    return DashaCalculationResponse(
        birth_utc_datetime_iso=resolved_time.utc_datetime_iso,
        target_utc_datetime_iso=_to_iso_utc(effective_target_utc),
        julian_day_ut=planet_res.julian_day_ut,
        ayanamsha_type=planet_res.ayanamsha_type,
        ayanamsha_value=planet_res.ayanamsha_value,
        year_length_days=DAYS_PER_YEAR,
        moon_longitude=round(moon_lon, 6),
        janma_nakshatra=moon_pos.nakshatra,
        janma_pada=moon_pos.pada,
        birth_dasha_lord=birth_dasha_lord,
        moon_elapsed_fraction=round(elapsed_fraction, 6),
        moon_remaining_fraction=round(remaining_fraction, 6),
        birth_balance_years=round(birth_balance_years, 6),
        birth_balance_days=round(birth_balance_days, 4),
        birth_balance_formatted=_format_balance_ymd(birth_balance_years),
        active_stack=active_stack,
        active_sookshma_periods=active_sookshma_list,
        active_prana_periods=active_prana_list,
        mahadashas=mahadashas,
    )
