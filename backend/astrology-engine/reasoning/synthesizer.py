from typing import List, Tuple
from evidence.evaluator import generate_astrological_evidence
from models.evidence import EvidenceGenerationRequest, EvidenceItemModel
from models.reasoning import (
    ReasoningStepModel,
    ReasoningSynthesisRequest,
    ReasoningSynthesisResponse,
)


def synthesize_astrological_reasoning(
    request: ReasoningSynthesisRequest,
) -> ReasoningSynthesisResponse:
    """
    Deterministically synthesize the Evidence Chain into a 5-step Reasoning DAG
    and strategic Jyotish conclusion with classical shastra citations.
    """
    # 1. Generate underlying deterministic evidence
    evidence_req = EvidenceGenerationRequest(
        date_of_birth=request.date_of_birth,
        time_of_birth=request.time_of_birth,
        latitude=request.latitude,
        longitude=request.longitude,
        timezone_id=request.timezone_id,
        question_text=request.question_text,
        question_category=request.question_category,
        ayanamsha_type=request.ayanamsha_type,
        node_type=request.node_type,
    )
    ev_res = generate_astrological_evidence(evidence_req)

    # 2. Partition evidence items by category
    house_items = [it for it in ev_res.evidence_items if it.category == "HOUSE_AND_LORD"]
    karaka_items = [it for it in ev_res.evidence_items if it.category == "KARAKA_STRENGTH"]
    varga_items = [it for it in ev_res.evidence_items if it.category == "DIVISIONAL_VARGA"]
    yoga_items = [it for it in ev_res.evidence_items if it.category == "YOGA_OR_DOSHA"]
    dasha_items = [it for it in ev_res.evidence_items if it.category == "DASHA_AND_GOCHAR"]

    steps: List[ReasoningStepModel] = []

    # -------------------------------------------------------------
    # Step 1: NATAL_PROMISE (Lagna, Karaka & Primary Bhava assessment)
    # -------------------------------------------------------------
    step1_items = house_items + karaka_items
    fav1 = sum(1 for it in step1_items if it.finding == "FAVORABLE")
    chal1 = sum(1 for it in step1_items if it.finding == "CHALLENGING")
    tot1 = max(len(step1_items), 1)

    if chal1 > fav1:
        v1 = "CHALLENGING"
        conf1 = 0.55 + 0.35 * (chal1 / tot1)
    elif fav1 >= chal1 and fav1 > 0:
        v1 = "FAVORABLE"
        conf1 = 0.60 + 0.35 * (fav1 / tot1)
    else:
        v1 = "MODERATE"
        conf1 = 0.60

    h_summary = "; ".join(it.observation for it in house_items[:2])
    k_summary = "; ".join(it.observation for it in karaka_items[:2])
    narrative_1 = (
        f"Evaluating the foundational birth chart (D1) promise for {ev_res.question_category}: "
        f"Primary Bhava capacity: {h_summary}. "
        f"Significator (Naisargika Karaka) strength: {k_summary}. "
        f"Verdict is {v1}, reflecting the natal baseline potential of the native."
    )

    steps.append(
        ReasoningStepModel(
            step_order=1,
            step_type="NATAL_PROMISE",
            title="Natal Promise & Bhava Bala Evaluation",
            verdict=v1,
            confidence_score=round(min(conf1, 0.95), 2),
            narrative=narrative_1,
            linked_factors=[it.factor for it in step1_items],
            shastra_citations=[
                "BPHS Ch. 28 (Bhava Bala Nirupana)",
                "BPHS Ch. 27 (Shadbala Adhyaya)",
                "BPHS Ch. 12-24 (House Lords & Significators)",
            ],
        )
    )

    # -------------------------------------------------------------
    # Step 2: DIVISIONAL_VALIDATION (Shodashavarga confirmation)
    # -------------------------------------------------------------
    fav2 = sum(1 for it in varga_items if it.finding == "FAVORABLE")
    chal2 = sum(1 for it in varga_items if it.finding == "CHALLENGING")
    tot2 = max(len(varga_items), 1)

    if chal2 > fav2:
        v2 = "CHALLENGING"
        conf2 = 0.55 + 0.30 * (chal2 / tot2)
    elif fav2 > 0:
        v2 = "FAVORABLE"
        conf2 = 0.65 + 0.25 * (fav2 / tot2)
    else:
        v2 = "MODERATE"
        conf2 = 0.60

    v_summary = "; ".join(it.observation for it in varga_items) if varga_items else "Divisional vargas operate neutrally."
    narrative_2 = (
        f"Validating D1 promise through subtle harmonic divisional charts ({', '.join(ev_res.required_vargas)}): "
        f"{v_summary} "
        f"According to Sage Parashara, a planet's strength in D1 must be corroborated in its specific Varga."
    )

    steps.append(
        ReasoningStepModel(
            step_order=2,
            step_type="DIVISIONAL_VALIDATION",
            title="Harmonic Varga Corroboration (D9 / D10 / Relevant Vargas)",
            verdict=v2,
            confidence_score=round(min(conf2, 0.95), 2),
            narrative=narrative_2,
            linked_factors=[it.factor for it in varga_items],
            shastra_citations=[
                "BPHS Ch. 7 (Shodashavarga Phala)",
                "Phaladeepika Ch. 3 (Varga Viveka)",
            ],
        )
    )

    # -------------------------------------------------------------
    # Step 3: YOGA_CATALYSTS (Active Yogas & Mitigating Doshas)
    # -------------------------------------------------------------
    fav3 = sum(1 for it in yoga_items if it.finding == "FAVORABLE")
    chal3 = sum(1 for it in yoga_items if it.finding == "CHALLENGING")

    if chal3 > fav3:
        v3 = "CHALLENGING"
        conf3 = 0.70
    elif fav3 > 0:
        v3 = "FAVORABLE"
        conf3 = 0.80
    else:
        v3 = "MODERATE"
        conf3 = 0.65

    y_summary = "; ".join(it.observation for it in yoga_items) if yoga_items else "Chart exhibits a steady baseline yoga structure."
    narrative_3 = (
        f"Inspecting classical planetary combinations and special yogas active in this domain: "
        f"{y_summary} "
        f"Auspicious Yogas amplify results and accelerate favorable karma, while afflicted combinations indicate areas for remedial care."
    )

    steps.append(
        ReasoningStepModel(
            step_order=3,
            step_type="YOGA_CATALYSTS",
            title="Classical Yoga & Dosha Catalysts",
            verdict=v3,
            confidence_score=round(conf3, 2),
            narrative=narrative_3,
            linked_factors=[it.factor for it in yoga_items],
            shastra_citations=[
                "BPHS Ch. 36-41 (Raja, Dhana & Nabhasa Yogas)",
                "Phaladeepika Ch. 6 (Yoga Phala)",
            ],
        )
    )

    # -------------------------------------------------------------
    # Step 4: TEMPORAL_TRIGGER (Dasha & Gochar Confluence Window)
    # -------------------------------------------------------------
    fav4 = sum(1 for it in dasha_items if it.finding == "FAVORABLE")
    chal4 = sum(1 for it in dasha_items if it.finding == "CHALLENGING")

    if fav4 > 0:
        v4 = "FAVORABLE"
        conf4 = 0.75
    elif chal4 > 0:
        v4 = "CHALLENGING"
        conf4 = 0.65
    else:
        v4 = "MODERATE"
        conf4 = 0.60

    d_summary = "; ".join(it.observation for it in dasha_items) if dasha_items else "Dasha and transit timings operate in baseline phase."
    narrative_4 = (
        f"Evaluating temporal trigger confluence (Vimshottari Dasha stack x Gochar transits): "
        f"{d_summary} "
        f"Transit of Jupiter and Saturn activates the karmic readiness of the chart."
    )

    steps.append(
        ReasoningStepModel(
            step_order=4,
            step_type="TEMPORAL_TRIGGER",
            title="Temporal Confluence (Vimshottari Dasha x Gochar Double Transit)",
            verdict=v4,
            confidence_score=round(conf4, 2),
            narrative=narrative_4,
            linked_factors=[it.factor for it in dasha_items],
            shastra_citations=[
                "Phaladeepika Ch. 19 (Dasha Phala Viveka)",
                "Phaladeepika Ch. 26 (Gochara Phala)",
            ],
        )
    )

    # -------------------------------------------------------------
    # Step 5: SYNTHESIS_AND_CONCLUSION (Verdict & Strategic Guidance)
    # -------------------------------------------------------------
    # Weights: Step 1 (30%), Step 2 (20%), Step 3 (20%), Step 4 (30%)
    score_map = {"FAVORABLE": 85.0, "MODERATE": 55.0, "CHALLENGING": 30.0}
    composite_score = (
        score_map[v1] * 0.30
        + score_map[v2] * 0.20
        + score_map[v3] * 0.20
        + score_map[v4] * 0.30
    )
    composite_score = round(composite_score, 1)

    if composite_score >= 68.0:
        overall_v = "FAVORABLE"
        verdict_text = "Strongly Auspicious & Favorable"
    elif composite_score >= 48.0:
        overall_v = "MODERATE_PROGRESS"
        verdict_text = "Steady Progress through Diligence & Structured Action"
    else:
        overall_v = "CHALLENGING"
        verdict_text = "Challenging Period Requiring Caution & Remedial Measures"

    narrative_5 = (
        f"Synthesis Verdict for '{ev_res.question_text}': {verdict_text} (Composite Score: {composite_score}/100). "
        f"The combination of Natal Promise ({v1}), Divisional Harmony ({v2}), "
        f"Yoga Influences ({v3}), and Current Planetary Confluence ({v4}) indicates that "
        f"focused efforts will yield proportional results in alignment with the active Vimshottari period."
    )

    # Determine classical remedies
    remedies = _recommend_classical_remedies(ev_res.question_category, chal1 + chal2 + chal3)

    steps.append(
        ReasoningStepModel(
            step_order=5,
            step_type="SYNTHESIS_AND_CONCLUSION",
            title="Synthesis Verdict & Strategic Guidance",
            verdict=overall_v if overall_v != "MODERATE_PROGRESS" else "MODERATE",
            confidence_score=round(composite_score / 100.0, 2),
            narrative=narrative_5,
            linked_factors=[it.factor for it in ev_res.evidence_items[:5]],
            shastra_citations=[
                "BPHS Ch. 85 (Shanti & Remedial Adhyaya)",
                "Brihat Jataka Ch. 20 (Niryana & Karmic Phala)",
            ],
        )
    )

    return ReasoningSynthesisResponse(
        framework_version=ev_res.framework_version,
        question_text=ev_res.question_text,
        question_category=ev_res.question_category,
        primary_houses=ev_res.primary_houses,
        overall_verdict=overall_v,
        composite_score=composite_score,
        reasoning_steps=steps,
        classical_remedies=remedies,
    )


def _recommend_classical_remedies(domain: str, affliction_count: int) -> List[str]:
    """
    Classical Parashari and Vedic remedial guidance (Parihara) tailored to domain and intensity.
    """
    base_remedies = {
        "CAREER_AND_PROFESSION": [
            "Recite Aditya Hridayam Stotram on Sundays at sunrise for professional authority and leadership grace.",
            "Offer Arghya (pure water with kumkum) to Surya Dev to strengthen career initiative and administrative standing.",
            "Practice righteous speech and maintain integrity in financial commitments on Thursdays (Guru Hora).",
        ],
        "MARRIAGE_AND_RELATIONSHIPS": [
            "Chant the Maha Mrityunjaya Mantra or Lakshmi-Narayana Kavacham for emotional harmony and marital peace.",
            "Offer white fragrant flowers or curd/milk to Shiva-Parvati on Fridays to strengthen Shukra (Venus).",
            "Maintain patience and open communication during transit Saturn or Mars aspects on the 7th house.",
        ],
        "WEALTH_AND_FINANCE": [
            "Recite Sri Suktam or Kanakadhara Stotram on Fridays to invoke auspicious Lakshmi blessings.",
            "Perform Annadana (food donation) to the needy on Saturdays to mitigate Saturnine delays in financial liquidity.",
            "Avoid speculative investments during periods when Mercury or Jupiter transits through dusthana houses.",
        ],
        "HEALTH_AND_LONGEVITY": [
            "Chant Maha Mrityunjaya Mantra 108 times daily facing East for vitality, physical protection, and longevity.",
            "Practice Pranayama and Surya Namaskar at dawn to invigorate Prana and strengthen lagnesha vitality.",
            "Engage in charitable donations of grains or sesame seeds on Saturdays (Shani Pradosham).",
        ],
    }

    selected = base_remedies.get(
        domain,
        [
            "Recite the Gayatri Mantra 108 times during Brahma Muhurta for mental clarity and spiritual alignment.",
            "Engage in regular charity (Dana) of yellow cloth or grains on Thursdays to honor Brihaspati (Jupiter).",
            "Maintain truthfulness, self-discipline (Sadhana), and respectful conduct toward elders and mentors.",
        ],
    )

    if affliction_count > 0:
        selected.append(
            "Perform Navagraha Shanti or propitiate afflicted house lords through simple devotional japa."
        )

    return selected
