"""
Deterministic Temporal Analysis & Dasha-Gochar Confluence Engine.
Synthesizes Natal Promise (Bhava Bala + Shadbala), Active 5-Level Vimshottari Dasha Lords (MD/AD/PD),
and Gochar Transits (including Double Transit of Jupiter + Saturn and Vedha) across a multi-window forecast horizon.
"""

from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional, Set, Tuple

from dashas.vimshottari import calculate_vimshottari_dasha
from houses.calculator import calculate_houses_and_ascendant
from models.dashas import DashaCalculationRequest
from models.strength import BhavaBalaCalculationRequest, ShadbalaCalculationRequest
from models.temporal import (
    DomainTimelineSummary,
    DomainWindowEvaluation,
    TemporalAnalysisRequest,
    TemporalAnalysisResponse,
    TemporalForecastWindow,
)
from models.transits import TransitCalculationRequest
from strength.bhavabala import calculate_bhava_bala
from strength.shadbala import calculate_shadbala_and_vimshopaka
from transits.gochar import calculate_gochar_transits


DOMAIN_DEFINITIONS: List[Dict[str, object]] = [
    {
        "code": "CAREER_AND_AUTHORITY",
        "title": "Career, Authority & Professional Growth (Karma & Artha)",
        "houses": [10, 6, 1, 11],
        "karakas": ["Sun", "Saturn", "Mercury", "Jupiter"],
    },
    {
        "code": "WEALTH_AND_ASSETS",
        "title": "Wealth, Income & Financial Prosperity (Dhana & Labha)",
        "houses": [2, 11, 5, 9],
        "karakas": ["Jupiter", "Venus", "Mercury"],
    },
    {
        "code": "MARRIAGE_AND_PARTNERSHIPS",
        "title": "Marriage, Relationships & Harmony (Kalatra & Kutumba)",
        "houses": [7, 2, 4, 11],
        "karakas": ["Venus", "Jupiter", "Moon"],
    },
    {
        "code": "HEALTH_AND_VITALITY",
        "title": "Health, Vitality & Resilience (Tanu & Ayur)",
        "houses": [1, 6, 8, 12],
        "karakas": ["Sun", "Moon", "Mars", "Saturn"],
    },
    {
        "code": "WISDOM_AND_SPIRITUALITY",
        "title": "Higher Learning, Fortune & Inner Purpose (Dharma & Moksha)",
        "houses": [5, 9, 4, 12],
        "karakas": ["Jupiter", "Ketu", "Sun", "Moon"],
    },
]


def _parse_anchor_utc(iso_str: Optional[str]) -> datetime:
    if not iso_str or not iso_str.strip():
        return datetime.now(timezone.utc).replace(hour=12, minute=0, second=0, microsecond=0)
    cleaned = iso_str.strip()
    if cleaned.endswith("Z"):
        cleaned = cleaned[:-1] + "+00:00"
    dt = datetime.fromisoformat(cleaned)
    if dt.tzinfo is None:
        return dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc)


def _format_window_label(start_dt: datetime, end_dt: datetime) -> str:
    s_str = start_dt.strftime("%b %Y")
    e_str = end_dt.strftime("%b %Y")
    if s_str == e_str:
        return start_dt.strftime("%d %b %Y") + " – " + end_dt.strftime("%d %b %Y")
    return f"{s_str} – {e_str}"


def _classify_score(score: float) -> str:
    if score >= 68.0:
        return "HIGH_OPPORTUNITY"
    if score >= 55.0:
        return "FAVORABLE_GROWTH"
    if score >= 42.0:
        return "STEADY_CONSOLIDATION"
    return "CAUTION_AND_REMEDY"


def calculate_temporal_analysis(request: TemporalAnalysisRequest) -> TemporalAnalysisResponse:
    """
    Compute deterministic multi-window Temporal Analysis combining Natal Promise (Bhava Bala + Shadbala),
    Active Vimshottari Dasha stack (MD/AD/PD), and Gochar Transits (Double Transit + Vedha + Sade Sati).
    """
    anchor_dt = _parse_anchor_utc(request.anchor_datetime_utc)
    anchor_iso = anchor_dt.strftime("%Y-%m-%dT%H:%M:%SZ")

    # 1. Calculate Natal Houses, Shadbala, and Bhava Bala once
    shadbala_res = calculate_shadbala_and_vimshopaka(
        ShadbalaCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )
    bhava_res = calculate_bhava_bala(
        BhavaBalaCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )

    shadbala_by_planet = {p.planet: p for p in shadbala_res.planets}
    bhava_by_house = {h.house_number: h for h in bhava_res.houses}

    # Build house lordship and occupation lookup from BhavaBala / Shadbala
    houses_ruled_by_planet: Dict[str, Set[int]] = {p.planet: set() for p in shadbala_res.planets}
    for h in bhava_res.houses:
        houses_ruled_by_planet.setdefault(h.lord_planet, set()).add(h.house_number)

    planet_natal_house: Dict[str, int] = {p.planet: p.house for p in shadbala_res.planets}

    # Pre-compute static Natal Promise score (0..100) for each domain
    domain_natal_scores: Dict[str, float] = {}
    for dom in DOMAIN_DEFINITIONS:
        d_code = str(dom["code"])
        d_houses: List[int] = list(dom["houses"])  # type: ignore
        d_karakas: List[str] = list(dom["karakas"])  # type: ignore

        # Average Bhava Bala Rupas across primary houses (typical Rupas ~ 6.5 to 10.5)
        avg_rupas = sum(bhava_by_house[h].total_bhava_bala_rupas for h in d_houses) / len(d_houses)
        house_component = min(100.0, max(30.0, (avg_rupas / 9.5) * 75.0))

        # Average Shadbala ratio of Karakas
        karaka_ratios = [
            shadbala_by_planet[k].shadbala_ratio
            for k in d_karakas
            if k in shadbala_by_planet
        ]
        avg_karaka_ratio = sum(karaka_ratios) / len(karaka_ratios) if karaka_ratios else 1.0
        karaka_component = min(100.0, max(30.0, avg_karaka_ratio * 65.0))

        domain_natal_scores[d_code] = round(0.60 * house_component + 0.40 * karaka_component, 1)

    # 2. Evaluate each forecast window
    step_days = request.window_Step_days
    timeline_windows: List[TemporalForecastWindow] = []
    natal_moon_sign = ""

    for w_idx in range(1, request.window_count + 1):
        w_start = anchor_dt + timedelta(days=(w_idx - 1) * step_days)
        w_end = w_start + timedelta(days=step_days)
        w_mid = w_start + timedelta(days=step_days / 2.0)

        w_start_iso = w_start.strftime("%Y-%m-%dT%H:%M:%SZ")
        w_end_iso = w_end.strftime("%Y-%m-%dT%H:%M:%SZ")
        w_mid_iso = w_mid.strftime("%Y-%m-%dT%H:%M:%SZ")
        w_label = _format_window_label(w_start, w_end)

        # Resolve active MD / AD / PD at window midpoint
        dasha_res = calculate_vimshottari_dasha(
            DashaCalculationRequest(
                date_of_birth=request.date_of_birth,
                time_of_birth=request.time_of_birth,
                latitude=request.latitude,
                longitude=request.longitude,
                timezone_id=request.timezone_id,
                target_datetime_iso=w_mid_iso,
                ayanamsha_type=request.ayanamsha_type,
                node_type=request.node_type,
            )
        )
        md_lord = dasha_res.active_stack[0].planet
        ad_lord = dasha_res.active_stack[1].planet
        pd_lord = dasha_res.active_stack[2].planet

        # Resolve Gochar transits at window midpoint
        transit_res = calculate_gochar_transits(
            TransitCalculationRequest(
                date_of_birth=request.date_of_birth,
                time_of_birth=request.time_of_birth,
                latitude=request.latitude,
                longitude=request.longitude,
                timezone_id=request.timezone_id,
                transit_datetime_utc=w_mid_iso,
                ayanamsha_type=request.ayanamsha_type,
                node_type=request.node_type,
            )
        )
        natal_moon_sign = transit_res.natal_moon_sign
        transit_by_planet = {tp.planet: tp for tp in transit_res.planets}
        activated_dt_houses = [
            dh.house_number for dh in transit_res.double_transit_houses if dh.is_activated
        ]
        activated_dt_set = set(activated_dt_houses)

        domain_evals: List[DomainWindowEvaluation] = []
        for dom in DOMAIN_DEFINITIONS:
            d_code = str(dom["code"])
            d_title = str(dom["title"])
            d_houses: List[int] = list(dom["houses"])  # type: ignore
            d_houses_set = set(d_houses)
            d_karakas: List[str] = list(dom["karakas"])  # type: ignore
            d_karakas_set = set(d_karakas)

            natal_score = domain_natal_scores[d_code]
            supporting: List[str] = []
            challenging: List[str] = []

            # A. Dasha Activation Score (0..100)
            dasha_score = 42.0
            for level_label, lord, weight in [
                ("Mahadasha", md_lord, 18.0),
                ("Antardasha", ad_lord, 14.0),
                ("Pratyantardasha", pd_lord, 8.0),
            ]:
                occ_h = planet_natal_house.get(lord, 1)
                ruled_h = houses_ruled_by_planet.get(lord, set())
                lord_ratio = (
                    shadbala_by_planet[lord].shadbala_ratio
                    if lord in shadbala_by_planet
                    else 1.0
                )

                connected_houses = ({occ_h} | ruled_h) & d_houses_set
                if connected_houses:
                    h_list_str = ", ".join(f"H{h}" for h in sorted(connected_houses))
                    dasha_score += weight * min(1.35, max(0.75, lord_ratio))
                    supporting.append(
                        f"{level_label} lord {lord} activates domain houses ({h_list_str}) with Shadbala ratio {lord_ratio:.2f}x."
                    )
                elif lord in d_karakas_set:
                    dasha_score += (weight * 0.65) * min(1.25, max(0.8, lord_ratio))
                    supporting.append(
                        f"{level_label} lord {lord} serves as natural Naisargika Karaka for {d_code.replace('_', ' ').title()}."
                    )

                # Check if Dasha lord exclusively occupies 8th or 12th Dusthana for non-spiritual/health domains
                if d_code not in ("WISDOM_AND_SPIRITUALITY", "HEALTH_AND_VITALITY") and occ_h in (8, 12) and not connected_houses:
                    dasha_score -= weight * 0.35
                    challenging.append(
                        f"{level_label} lord {lord} is placed in Natal H{occ_h} (Dusthana), requiring patience."
                    )

            dasha_score = round(min(96.0, max(24.0, dasha_score)), 1)

            # B. Transit Confluence Score (0..100)
            transit_score = 40.0
            dt_overlap = sorted(d_houses_set & activated_dt_set)
            double_transit_triggered = len(dt_overlap) > 0
            if double_transit_triggered:
                overlap_str = ", ".join(f"H{h}" for h in dt_overlap)
                transit_score += 22.0 + (4.0 * min(2, len(dt_overlap) - 1))
                supporting.append(
                    f"Jupiter + Saturn Double Transit simultaneously activates Natal {overlap_str}."
                )

            # Check Gochar of active MD/AD lords and domain Karakas
            key_planets_to_check = list(dict.fromkeys([md_lord, ad_lord] + d_karakas[:2]))
            for kp in key_planets_to_check:
                tp = transit_by_planet[kp]
                if tp.gochar_status == "FAVORABLE":
                    transit_score += 7.5
                    supporting.append(
                        f"Transiting {kp} in {tp.transit_sign} (H{tp.house_from_moon} from Moon) is in unobstructed benefic Gochar ({tp.tara_bala_category} Tara)."
                    )
                elif tp.gochar_status == "VEDHA_OBSTRUCTED":
                    transit_score -= 3.0
                    challenging.append(
                        f"Transiting {kp} in H{tp.house_from_moon} from Moon is obstructed via Vedha by {tp.vedha_obstructor}."
                    )
                elif tp.house_from_lagna in d_houses_set:
                    transit_score += 4.5
                    supporting.append(
                        f"Transiting {kp} traverses Natal H{tp.house_from_lagna} ({tp.transit_sign}), energizing this domain."
                    )

            if transit_res.sade_sati.sade_sati_active or transit_res.sade_sati.dhaiya_active:
                if d_code in ("HEALTH_AND_VITALITY", "MARRIAGE_AND_PARTNERSHIPS"):
                    transit_score -= 5.0
                challenging.append(
                    f"Shani {transit_res.sade_sati.phase.replace('_', ' ')} in {transit_res.sade_sati.saturn_transit_sign} demands disciplined pacing."
                )

            transit_score = round(min(96.0, max(22.0, transit_score)), 1)

            if not supporting:
                supporting.append(
                    f"Baseline Natal Bhava Bala ({natal_score:.1f}/100) provides steady structural support."
                )
            if not challenging:
                challenging.append(
                    "No major Vedha obstructions or Dusthana afflictions detected in this window."
                )

            overall_score = round(
                (0.30 * natal_score) + (0.40 * dasha_score) + (0.30 * transit_score),
                1,
            )
            classification = _classify_score(overall_score)

            domain_evals.append(
                DomainWindowEvaluation(
                    domain_code=d_code,
                    domain_title=d_title,
                    primary_houses=d_houses,
                    natal_promise_score=natal_score,
                    dasha_activation_score=dasha_score,
                    transit_confluence_score=transit_score,
                    overall_confluence_score=overall_score,
                    window_classification=classification,
                    double_transit_triggered=double_transit_triggered,
                    supporting_factors=supporting[:4],
                    challenging_factors=challenging[:3],
                )
            )

        best_dom_in_window = max(domain_evals, key=lambda d: d.overall_confluence_score)
        avg_window_score = round(
            sum(d.overall_confluence_score for d in domain_evals) / len(domain_evals),
            1,
        )

        timeline_windows.append(
            TemporalForecastWindow(
                window_index=w_idx,
                window_label=w_label,
                start_utc=w_start_iso,
                end_utc=w_end_iso,
                midpoint_utc=w_mid_iso,
                mahadasha_lord=md_lord,
                antardasha_lord=ad_lord,
                pratyantardasha_lord=pd_lord,
                jupiter_transit_sign=transit_by_planet["Jupiter"].transit_sign,
                saturn_transit_sign=transit_by_planet["Saturn"].transit_sign,
                sade_sati_phase=transit_res.sade_sati.phase,
                double_transit_houses=activated_dt_houses,
                overall_window_score=avg_window_score,
                dominant_domain=best_dom_in_window.domain_code,
                domain_evaluations=domain_evals,
            )
        )

    # 3. Build Domain Timeline Summaries across all windows
    domain_summaries: List[DomainTimelineSummary] = []
    for dom in DOMAIN_DEFINITIONS:
        d_code = str(dom["code"])
        d_title = str(dom["title"])
        d_houses: List[int] = list(dom["houses"])  # type: ignore
        d_karakas: List[str] = list(dom["karakas"])  # type: ignore

        matching_pairs: List[Tuple[TemporalForecastWindow, DomainWindowEvaluation]] = []
        for w in timeline_windows:
            ev = next(e for e in w.domain_evaluations if e.domain_code == d_code)
            matching_pairs.append((w, ev))

        avg_conf = round(
            sum(ev.overall_confluence_score for _, ev in matching_pairs) / len(matching_pairs),
            1,
        )
        peak_w, peak_ev = max(matching_pairs, key=lambda pair: pair[1].overall_confluence_score)
        current_ev = matching_pairs[0][1]

        exec_summary = (
            f"Peak confluence ({peak_ev.overall_confluence_score:.1f}/100 • {peak_ev.window_classification.replace('_', ' ')}) "
            f"occurs in {peak_w.window_label} under {peak_w.mahadasha_lord}–{peak_w.antardasha_lord}–{peak_w.pratyantardasha_lord} Dasha."
        )

        domain_summaries.append(
            DomainTimelineSummary(
                domain_code=d_code,
                domain_title=d_title,
                primary_houses=d_houses,
                karaka_planets=d_karakas,
                natal_promise_score=domain_natal_scores[d_code],
                average_confluence_score=avg_conf,
                peak_score=peak_ev.overall_confluence_score,
                peak_window_label=peak_w.window_label,
                peak_window_start_utc=peak_w.start_utc,
                peak_window_end_utc=peak_w.end_utc,
                current_classification=current_ev.window_classification,
                executive_summary=exec_summary,
            )
        )

    best_window = max(timeline_windows, key=lambda w: w.overall_window_score)
    strongest_domain = max(domain_summaries, key=lambda s: s.average_confluence_score)

    return TemporalAnalysisResponse(
        natal_utc_datetime_iso=shadbala_res.utc_datetime_iso,
        anchor_utc_datetime_iso=anchor_iso,
        natal_ascendant_sign=shadbala_res.ascendant_sign,
        natal_moon_sign=natal_moon_sign,
        ayanamsha_type=shadbala_res.ayanamsha_type,
        window_count=len(timeline_windows),
        best_overall_window_label=best_window.window_label,
        strongest_domain_code=strongest_domain.domain_code,
        domain_summaries=domain_summaries,
        timeline_windows=timeline_windows,
    )
