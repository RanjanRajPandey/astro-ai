"""
Deterministic Evidence Engine & Shastra Observation Pipeline.
Executes Classical BPHS / Phaladeepika Checklist Rules against computed Astrological Primitives
and outputs structured, fully traceable EvidenceItem records for the Reasoning Engine and AI Service.
"""

from typing import Dict, List, Optional
from datetime import datetime, timezone

from divisional.shodashavarga import calculate_shodashavarga
from frameworks.classifier import classify_question_and_load_framework
from models.dashas import DashaCalculationRequest
from models.divisional import DivisionalCalculationRequest
from models.evidence import (
    EvidenceGenerationRequest,
    EvidenceGenerationResponse,
    EvidenceItemModel,
)
from models.frameworks import QuestionClassificationRequest
from models.strength import BhavaBalaCalculationRequest, ShadbalaCalculationRequest
from models.transits import TransitCalculationRequest
from models.yogas import YogaCalculationRequest
from strength.bhavabala import calculate_bhava_bala
from strength.shadbala import calculate_shadbala_and_vimshopaka
from transits.gochar import calculate_gochar_transits
from yogas.detector import detect_all_yogas
from dashas.vimshottari import calculate_vimshottari_dasha


def generate_astrological_evidence(
    request: EvidenceGenerationRequest,
) -> EvidenceGenerationResponse:
    """
    Deterministically generate comprehensive, verifiable EvidenceItems based on the active consultation framework.
    """
    # 1. Resolve Framework from Question Text or explicit category
    classify_res = classify_question_and_load_framework(
        QuestionClassificationRequest(question_text=request.question_text)
    )
    if request.question_category:
        matching_fw = next(
            (f for f in classify_res.all_frameworks if f.category_code == request.question_category),
            classify_res.active_framework,
        )
        active_framework = matching_fw
        active_category = matching_fw.category_code
    else:
        active_framework = classify_res.active_framework
        active_category = classify_res.primary_category

    # 2. Compute all foundational primitives deterministically
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
    bhava_by_num = {h.house_number: h for h in bhava_res.houses}

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
    shadbala_by_planet = {p.planet: p for p in shadbala_res.planets}

    divisional_res = calculate_shodashavarga(
        DivisionalCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )
    varga_by_code = {v.varga_code: v for v in divisional_res.charts}

    yoga_res = detect_all_yogas(
        YogaCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )

    dasha_res = calculate_vimshottari_dasha(
        DashaCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            target_datetime_iso=request.target_datetime_utc,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )

    transit_res = calculate_gochar_transits(
        TransitCalculationRequest(
            date_of_birth=request.date_of_birth,
            time_of_birth=request.time_of_birth,
            latitude=request.latitude,
            longitude=request.longitude,
            timezone_id=request.timezone_id,
            transit_datetime_utc=request.target_datetime_utc,
            ayanamsha_type=request.ayanamsha_type,
            node_type=request.node_type,
        )
    )
    transit_by_planet = {tp.planet: tp for tp in transit_res.planets}
    activated_dt_houses = {
        dh.house_number: dh for dh in transit_res.double_transit_houses if dh.is_activated
    }

    # 3. Generate structured Evidence Items based on Framework rules & verified facts
    items: List[EvidenceItemModel] = []

    # Category A: Primary & Secondary House Strengths
    for h_num in active_framework.primary_houses:
        h = bhava_by_num[h_num]
        lord = h.lord_planet
        lord_st = shadbala_by_planet[lord]
        is_strong = h.total_bhava_bala_rupas >= 7.5
        finding = "FAVORABLE" if is_strong else ("CHALLENGING" if h.total_bhava_bala_rupas < 6.0 else "NEUTRAL")
        obs = (
            f"House {h_num} ({h.sign} / {h.sanskrit_sign}) has {h.total_bhava_bala_rupas:.2f} Rupas "
            f"({h.strength_grade}), ruled by {lord} ({lord_st.d1_dignity}, placed in H{lord_st.house}, "
            f"Shadbala ratio {lord_st.shadbala_ratio:.2f}x). Occupants: {', '.join(h.occupants) if h.occupants else 'None'}."
        )
        items.append(
            EvidenceItemModel(
                factor=f"Primary House {h_num} ({h.domain_title}) Strength",
                category="HOUSE_AND_LORD",
                observation=obs,
                rule_reference=f"BPHS Ch. 28 (Bhava Bala) & Ch. 12-24 (House Significators)",
                finding=finding,
                weight=0.25,
            )
        )

    # Category B: Naisargika Karaka Strengths
    for karaka in active_framework.naisargika_karakas:
        if karaka not in shadbala_by_planet:
            continue
        st = shadbala_by_planet[karaka]
        is_favorable = st.shadbala_ratio >= 1.0 and not (karaka != "Sun" and st.house == 8)
        finding = "FAVORABLE" if is_favorable else ("CHALLENGING" if st.shadbala_ratio < 0.9 else "NEUTRAL")
        obs = (
            f"Naisargika Karaka {karaka} possesses {st.total_shadbala_rupas:.2f} Rupas "
            f"(Shadbala ratio: {st.shadbala_ratio:.2f}x, Grade: {st.strength_grade}, D1 Dignity: {st.d1_dignity} in {st.sign} H{st.house}). "
            f"Vimshopaka Bala is {st.vimshopaka_bala:.1f}/20 ({st.vimshopaka_percentage:.1f}%)."
        )
        items.append(
            EvidenceItemModel(
                factor=f"Karaka {karaka} Strength & Vimshopaka Bala",
                category="KARAKA_STRENGTH",
                observation=obs,
                rule_reference="BPHS Ch. 27 (Shadbala Nirupana) & Ch. 32 (Karaka Adhyaya)",
                finding=finding,
                weight=0.20,
            )
        )

    # Category C: Divisional Varga Placements (D9, D10, D2, D7, etc.)
    for v_code in active_framework.required_vargas:
        if v_code == "D1" or v_code not in varga_by_code:
            continue
        v_chart = varga_by_code[v_code]
        primary_h = active_framework.primary_houses[0]
        # Inspect primary house lord in this Varga
        primary_lord = bhava_by_num[primary_h].lord_planet
        v_planet = next((p for p in v_chart.planets if p.planet == primary_lord), None)
        v_lagna = v_chart.ascendant_sign
        if v_planet:
            finding = "FAVORABLE" if v_planet.dignity_in_varga in ("EXALTED", "OWN_SIGN", "MOOLATRIKONA", "FRIEND", "GREAT_FRIEND") else "NEUTRAL"
            obs = (
                f"In {v_chart.title} ({v_code}), Lagna is {v_lagna}. D1 {primary_h}th Lord {primary_lord} "
                f"is placed in {v_planet.varga_sign} (H{v_planet.varga_house} in {v_code}) with dignity {v_planet.dignity_in_varga.replace('_', ' ')}."
            )
        else:
            obs = f"In {v_chart.title} ({v_code}), Lagna is {v_lagna}."

        items.append(
            EvidenceItemModel(
                factor=f"{v_code} Divisional Chart Placement",
                category="DIVISIONAL_VARGA",
                observation=obs,
                rule_reference=f"BPHS Ch. 7 (Shodashavarga Phala) & Phaladeepika Ch. 3",
                finding=finding,
                weight=0.20,
            )
        )

    # Category D: Active Classical Yogas & Doshas
    active_domain_yogas = [
        y for y in yoga_res.all_evaluated_yogas
        if y.status == "ACTIVE" and (
            y.yoga_code in active_framework.key_yogas_to_check
            or set(y.houses_involved) & set(active_framework.primary_houses)
        )
    ]
    if active_domain_yogas:
        for yg in active_domain_yogas[:2]:
            effect_desc = getattr(yg, "classical_Effect", getattr(yg, "classical_effect", ""))
            items.append(
                EvidenceItemModel(
                    factor=f"Active Classical Yoga: {yg.name}",
                    category="YOGA_OR_DOSHA",
                    observation=(
                        f"{yg.name} ({yg.sanskrit_name}) is active with {yg.strength} strength. "
                        f"Involved Grahas: {', '.join(yg.planets_involved)}. Phala: {effect_desc}"
                    ),
                    rule_reference="Brihat Parashara Hora Shastra Ch. 36-41 & Phaladeepika Ch. 6",
                    finding="FAVORABLE" if yg.is_benefic else "CHALLENGING",
                    weight=0.15,
                )
            )
    else:
        items.append(
            EvidenceItemModel(
                factor="Baseline Yoga Environment",
                category="YOGA_OR_DOSHA",
                observation="No adverse Doshas directly afflict the primary houses of this domain; baseline chart yoga matrix operates stably.",
                rule_reference="BPHS Ch. 36-41",
                finding="NEUTRAL",
                weight=0.10,
            )
        )

    # Category E: Vimshottari Dasha & Gochar Confluence
    md = dasha_res.active_stack[0].planet
    ad = dasha_res.active_stack[1].planet
    pd = dasha_res.active_stack[2].planet

    dt_hits = [h for h in active_framework.primary_houses if h in activated_dt_houses]
    dt_favorable = len(dt_hits) > 0
    dasha_confluence = "FAVORABLE" if dt_favorable or (md in active_framework.naisargika_karakas or ad in active_framework.naisargika_karakas) else "NEUTRAL"
    dasha_obs = (
        f"Active Vimshottari Dasha stack is {md} (MD) → {ad} (AD) → {pd} (PD). "
        f"Transiting Jupiter is in {transit_by_planet['Jupiter'].transit_sign}, Saturn in {transit_by_planet['Saturn'].transit_sign}. "
    )
    if dt_favorable:
        dasha_obs += f"Double Transit simultaneously activates primary Natal House(s): {', '.join(f'H{h}' for h in dt_hits)}."
    else:
        dasha_obs += "Double Transit operates on other houses, requiring steady foundational preparation."

    items.append(
        EvidenceItemModel(
            factor="Vimshottari Dasha & Gochar Double Transit Confluence",
            category="DASHA_AND_GOCHAR",
            observation=dasha_obs,
            rule_reference="Phaladeepika Ch. 19 (Dasha Phala) & Ch. 26 (Gochar Phala)",
            finding=dasha_confluence,
            weight=0.20,
        )
    )

    favorable_c = sum(1 for it in items if it.finding == "FAVORABLE")
    challenging_c = sum(1 for it in items if it.finding == "CHALLENGING")
    neutral_c = sum(1 for it in items if it.finding == "NEUTRAL")

    factors_considered = [it.factor for it in items]
    time_windows_summary = [
        f"Active Dasha: {md}-{ad}-{pd} ({dasha_res.target_utc_datetime_iso[:10]})",
        f"Double Transit: Jupiter ({transit_by_planet['Jupiter'].transit_sign}) + Saturn ({transit_by_planet['Saturn'].transit_sign})",
    ]

    return EvidenceGenerationResponse(
        framework_version="1.0.0-BPHS-SHODASHAVARGA",
        question_text=classify_res.question_text,
        question_category=active_category,
        primary_houses=active_framework.primary_houses,
        required_vargas=active_framework.required_vargas,
        total_evidence_count=len(items),
        favorable_count=favorable_c,
        challenging_count=challenging_c,
        neutral_count=neutral_c,
        evidence_items=items,
        factors_considered=factors_considered,
        time_windows_summary=time_windows_summary,
    )
