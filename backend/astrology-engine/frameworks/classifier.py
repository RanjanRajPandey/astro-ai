"""
Deterministic Question Classifier & Classical Vedic Analysis Framework Loader.
Maps natural-language user questions to structured BPHS / Phaladeepika multi-chart evaluation frameworks
used by the Evidence Engine (Phase 16), Reasoning Engine (Phase 17), and AI Interpretation Layer (Phase 18/19).
"""

import re
from typing import Dict, List, Optional, Tuple
from models.frameworks import (
    AnalysisFrameworkDefinition,
    FrameworkChecklistRule,
    QuestionClassificationRequest,
    QuestionClassificationResponse,
)

FRAMEWORK_VERSION = "1.0.0-BPHS-SHODASHAVARGA"

FRAMEWORK_CATALOG: List[AnalysisFrameworkDefinition] = [
    AnalysisFrameworkDefinition(
        category_code="CAREER_AND_PROFESSION",
        title="Career, Profession & Public Authority",
        sanskrit_title="Karma & Rajya Bhava Vichara",
        description=(
            "Evaluates professional trajectory, promotions, leadership authority, business vs. service suitability, "
            "and career timing using the 10th/6th/11th houses, D10 Dashamsha, Shadbala, and Double Transit."
        ),
        primary_houses=[10, 6, 11],
        secondary_houses=[1, 2, 9],
        required_vargas=["D1", "D9", "D10"],
        naisargika_karakas=["Sun", "Saturn", "Mercury", "Jupiter"],
        special_lagnas=["Arudha Lagna (AL)", "Udaya Lagna"],
        key_yogas_to_check=[
            "DHARMA_KARMADHIPATI_YOGA",
            "KENDRA_TRIKONA_RAJA_YOGA",
            "RUCHAKA_YOGA",
            "BHADRA_YOGA",
            "HAMSA_YOGA",
            "SHASHA_YOGA",
            "BUDHADITYA_YOGA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="CAR_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 10th house (Karma Bhava) Bhava Bala, occupants, and 10th lord placement & dignity in D1.",
                classical_reference="BPHS Ch. 21 & Phaladeepika Ch. 5",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="CAR_02",
                factor_category="DIVISIONAL_VARGA",
                description="Verify D10 (Dashamsha) Lagna lord, 10th house, and D1-to-D10 dignity of the 10th lord.",
                classical_reference="BPHS Ch. 7 (Shodashavarga Phala)",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="CAR_03",
                factor_category="KARAKA_STRENGTH",
                description="Evaluate Shadbala ratio and Vimshopaka Bala of Karma Karakas (Sun, Saturn, Mercury, Jupiter).",
                classical_reference="BPHS Ch. 27 (Shadbala) & Ch. 32 (Karaka)",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="CAR_04",
                factor_category="YOGA_OR_DOSHA",
                description="Check active Pancha Mahapurusha, Dharma-Karmadhipati (9-10), and Raja Yogas involving the 10th/1st/9th houses.",
                classical_reference="BPHS Ch. 36-41 (Raja Yogas)",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="CAR_05",
                factor_category="DASHA_AND_GOCHAR",
                description="Assess active MD/AD/PD connection to H10/H6/H11 and Jupiter + Saturn Double Transit on H10.",
                classical_reference="Phaladeepika Ch. 19 & 26",
                weight=0.20,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="MARRIAGE_AND_RELATIONSHIPS",
        title="Marriage, Spouse & Relationship Harmony",
        sanskrit_title="Vivaha & Kalatra Bhava Vichara",
        description=(
            "Evaluates marriage timing, partner qualities, marital harmony, and relationship longevity using the 7th/2nd/4th/8th/12th houses, "
            "D9 Navamsha, Venus/Jupiter strength, Upapada Lagna (UL), and Kuja Dosha Parihara."
        ),
        primary_houses=[7, 2, 4],
        secondary_houses=[8, 12, 11, 5],
        required_vargas=["D1", "D9", "D30"],
        naisargika_karakas=["Venus", "Jupiter", "Moon"],
        special_lagnas=["Upapada Lagna (UL)", "Chandra Lagna"],
        key_yogas_to_check=[
            "MALAVYA_YOGA",
            "GAJAKESARI_YOGA",
            "MANGLIK_KUJA_DOSHA",
            "NEECHA_BHANGA_RAJA_YOGA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="MAR_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 7th house (Kalatra Bhava) Bhava Bala, occupants, aspects, and 7th lord dignity in D1.",
                classical_reference="BPHS Ch. 18 (Saptama Bhava Phala)",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="MAR_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D9 (Navamsha) Lagna, D9 7th house, and Navamsha dignity of D1 7th lord and Venus.",
                classical_reference="Phaladeepika Ch. 10 & BPHS Ch. 7",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="MAR_03",
                factor_category="KARAKA_STRENGTH",
                description="Assess Kalatra Karaka Venus (and Jupiter) Shadbala, combustion, and Upapada Lagna (UL) lord.",
                classical_reference="BPHS Ch. 30 (Upapada Adhyaya)",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="MAR_04",
                factor_category="YOGA_OR_DOSHA",
                description="Evaluate Manglik / Kuja Dosha from Lagna, Moon, and Venus along with classical Parihara (cancellation).",
                classical_reference="Brihat Parashara Hora Shastra Ch. 81",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="MAR_05",
                factor_category="DASHA_AND_GOCHAR",
                description="Check active Dasha connection to H7/H2/UL and Double Transit of Jupiter + Saturn on H7 or 7th lord.",
                classical_reference="Phaladeepika Ch. 26 (Gochar)",
                weight=0.20,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="WEALTH_AND_FINANCE",
        title="Wealth, Income, Assets & Financial Prosperity",
        sanskrit_title="Dhana & Labha Bhava Vichara",
        description=(
            "Evaluates accumulated wealth, cash flow, investments, and financial abundance using the 2nd/11th/5th/9th houses, "
            "D2 Hora, Dhana Yogas, Indu/Arudha Lagna, and Jupiter/Venus strength."
        ),
        primary_houses=[2, 11, 5, 9],
        secondary_houses=[1, 10, 8, 12],
        required_vargas=["D1", "D2", "D9", "D16"],
        naisargika_karakas=["Jupiter", "Venus", "Mercury"],
        special_lagnas=["Arudha Lagna (AL)", "Udaya Lagna"],
        key_yogas_to_check=[
            "DHANA_YOGA_SAMBANDHA",
            "LAKSHMI_YOGA",
            "CHANDRA_MANGALA_YOGA",
            "GAJAKESARI_YOGA",
            "ADHI_YOGA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="WLTH_01",
                factor_category="HOUSE_AND_LORD",
                description="Evaluate 2nd (Dhana) and 11th (Labha) houses, their lords, and trinal support from 5th and 9th lords.",
                classical_reference="BPHS Ch. 13 & Ch. 22",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="WLTH_02",
                factor_category="DIVISIONAL_VARGA",
                description="Inspect D2 (Parashari Hora — Surya vs Chandra Hora) and D9 Navamsha placement of Dhana lords.",
                classical_reference="BPHS Ch. 6 & Ch. 7",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="WLTH_03",
                factor_category="KARAKA_STRENGTH",
                description="Examine Dhana Karaka Jupiter and Labha/Luxury Karaka Venus Shadbala & Vimshopaka Bala.",
                classical_reference="BPHS Ch. 27",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="WLTH_04",
                factor_category="YOGA_OR_DOSHA",
                description="Verify active Dhana Yogas, Lakshmi Yoga, Chandra-Mangala, and absence of uncancelled Kemadruma.",
                classical_reference="BPHS Ch. 41 (Dhana Yogas)",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="WLTH_05",
                factor_category="DASHA_AND_GOCHAR",
                description="Assess active Dasha of 2nd/11th/5th/9th lords and Jupiter Gochar in 2/5/7/9/11 from Natal Moon.",
                classical_reference="Phaladeepika Ch. 26",
                weight=0.20,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="HEALTH_AND_LONGEVITY",
        title="Health, Vitality, Recovery & Longevity",
        sanskrit_title="Tanu, Roga & Ayur Bhava Vichara",
        description=(
            "Evaluates physical constitution, immunity, stress resilience, and recovery windows using the 1st/6th/8th/12th houses, "
            "D3 Drekkana, D30 Trimshamsha, Lagna Lord Shadbala, and Sade Sati / Dhaiya transits."
        ),
        primary_houses=[1, 6, 8],
        secondary_houses=[12, 4],
        required_vargas=["D1", "D3", "D9", "D30"],
        naisargika_karakas=["Sun", "Moon", "Saturn", "Mars"],
        special_lagnas=["Udaya Lagna", "Chandra Lagna"],
        key_yogas_to_check=[
            "VIPARITA_HARSHA_YOGA",
            "VIPARITA_SARALA_YOGA",
            "VIPARITA_VIMALA_YOGA",
            "GRAHAN_DOSHA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="HLTH_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 1st house (Tanu Bhava) and Lagna lord strength vs. 6th (Roga) and 8th (Ayur) house afflictions.",
                classical_reference="BPHS Ch. 12 & Ch. 17",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="HLTH_02",
                factor_category="DIVISIONAL_VARGA",
                description="Check D30 (Trimshamsha) and D3 (Drekkana) placements of Lagna lord, Sun, Moon, and 6th/8th lords.",
                classical_reference="BPHS Ch. 7",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="HLTH_03",
                factor_category="KARAKA_STRENGTH",
                description="Evaluate Atma/Vitality Karaka Sun, Manas Karaka Moon, and Ayush Karaka Saturn Shadbala.",
                classical_reference="BPHS Ch. 27 & Ch. 43",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="HLTH_04",
                factor_category="YOGA_OR_DOSHA",
                description="Check Viparita Raja Yogas (Harsha/Sarala) and eclipse/nodal afflictions (Grahan Dosha).",
                classical_reference="Phaladeepika Ch. 6",
                weight=0.10,
            ),
            FrameworkChecklistRule(
                rule_code="HLTH_05",
                factor_category="DASHA_AND_GOCHAR",
                description="Examine Dasha of 6th/8th/12th lords and Saturn Sade Sati / Ashtama Shani transit phases.",
                classical_reference="Phaladeepika Ch. 26",
                weight=0.20,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="EDUCATION_AND_INTELLECT",
        title="Education, Higher Learning & Intellectual Mastery",
        sanskrit_title="Vidya, Dhi & Bhagya Vichara",
        description=(
            "Evaluates academic excellence, competitive exams, research aptitude, and wisdom using the 4th/5th/9th/2nd houses, "
            "D24 Chaturvimshamsha (Siddhamsha), and Mercury/Jupiter strength."
        ),
        primary_houses=[4, 5, 9],
        secondary_houses=[2, 1, 11],
        required_vargas=["D1", "D9", "D24"],
        naisargika_karakas=["Mercury", "Jupiter", "Moon"],
        special_lagnas=["Udaya Lagna", "Chandra Lagna"],
        key_yogas_to_check=[
            "BUDHADITYA_YOGA",
            "BHADRA_YOGA",
            "HAMSA_YOGA",
            "GAJAKESARI_YOGA",
            "SARASWATI_OR_ADHI_YOGA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="EDU_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 4th (Foundational Vidya), 5th (Dhi / Intelligence), and 9th (Higher Wisdom) houses and lords.",
                classical_reference="BPHS Ch. 15, 16 & 20",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="EDU_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D24 (Chaturvimshamsha / Siddhamsha) Lagna, 4th/5th houses, and Mercury/Jupiter dignity.",
                classical_reference="BPHS Ch. 7",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="EDU_03",
                factor_category="KARAKA_STRENGTH",
                description="Verify Vidya Karaka Mercury and Dhi Karaka Jupiter Shadbala and freedom from deep combustion.",
                classical_reference="BPHS Ch. 27",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="EDU_04",
                factor_category="YOGA_OR_DOSHA",
                description="Check Budhaditya, Bhadra, Hamsa, and Gajakesari Yogas supporting scholarly mastery.",
                classical_reference="Phaladeepika Ch. 6",
                weight=0.15,
            ),
            FrameworkChecklistRule(
                rule_code="EDU_05",
                factor_category="DASHA_AND_GOCHAR",
                description="Assess active Dasha of 4th/5th/9th lords and Jupiter transit influence on Natal 5th/9th houses.",
                classical_reference="Phaladeepika Ch. 19 & 26",
                weight=0.15,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="PROPERTY_AND_VEHICLES",
        title="Real Estate, Home, Land & Vehicles",
        sanskrit_title="Bhumi, Griha & Vahana Vichara",
        description=(
            "Evaluates real estate acquisition, domestic comfort, and luxury conveyances using the 4th/2nd/11th houses, "
            "D4 Chaturthamsha, D16 Shodashamsha, Mars (Bhumi Karaka), and Venus (Vahana Karaka)."
        ),
        primary_houses=[4, 11, 2],
        secondary_houses=[12, 9, 1],
        required_vargas=["D1", "D4", "D16", "D9"],
        naisargika_karakas=["Mars", "Venus", "Moon", "Saturn"],
        special_lagnas=["Arudha Lagna (AL)", "Udaya Lagna"],
        key_yogas_to_check=["MALAVYA_YOGA", "RUCHAKA_YOGA", "GAJAKESARI_YOGA"],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="PROP_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 4th house (Sukha & Griha Bhava) Bhava Bala, occupants, and 4th lord placement.",
                classical_reference="BPHS Ch. 15",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="PROP_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D4 (Chaturthamsha for fixed assets/land) and D16 (Shodashamsha for vehicles/comforts).",
                classical_reference="BPHS Ch. 7",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="PROP_03",
                factor_category="KARAKA_STRENGTH",
                description="Evaluate Bhumi Karaka Mars and Vahana Karaka Venus Shadbala & Vimshopaka Bala.",
                classical_reference="BPHS Ch. 27 & 32",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="PROP_04",
                factor_category="DASHA_AND_GOCHAR",
                description="Check active Dasha of 4th/11th/2nd lords and Double Transit activation on the 4th house.",
                classical_reference="Phaladeepika Ch. 19 & 26",
                weight=0.25,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="CHILDREN_AND_PROGENY",
        title="Children, Progeny & Creative Legacy",
        sanskrit_title="Santana & Putra Bhava Vichara",
        description=(
            "Evaluates childbirth timing, progeny well-being, and creative legacy using the 5th/9th/2nd/11th houses, "
            "D7 Saptamsha, and Putra Karaka Jupiter."
        ),
        primary_houses=[5, 9, 11],
        secondary_houses=[2, 1, 7],
        required_vargas=["D1", "D7", "D9"],
        naisargika_karakas=["Jupiter", "Moon", "Venus"],
        special_lagnas=["Udaya Lagna", "Chandra Lagna"],
        key_yogas_to_check=["HAMSA_YOGA", "GAJAKESARI_YOGA", "KENDRA_TRIKONA_RAJA_YOGA"],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="CHLD_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 5th house (Putra Bhava) Bhava Bala, occupants, and 5th lord dignity in D1.",
                classical_reference="BPHS Ch. 16 (Panchama Bhava Phala)",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="CHLD_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D7 (Saptamsha) Lagna, 5th house, and D7 placement of D1 5th lord and Jupiter.",
                classical_reference="BPHS Ch. 7",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="CHLD_03",
                factor_category="KARAKA_STRENGTH",
                description="Evaluate Putra Karaka Jupiter Shadbala, dignity, and benefic aspects.",
                classical_reference="BPHS Ch. 27 & 32",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="CHLD_04",
                factor_category="DASHA_AND_GOCHAR",
                description="Verify active Dasha of 5th/9th/11th lords and Double Transit activation on Natal 5th or 9th house.",
                classical_reference="Phaladeepika Ch. 26",
                weight=0.20,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="FOREIGN_TRAVEL_AND_SETTLEMENT",
        title="Foreign Travel, Relocation & Global Opportunities",
        sanskrit_title="Pravasa & Videsha Vichara",
        description=(
            "Evaluates international travel, overseas education/career, and permanent foreign settlement using the 12th/9th/3rd/7th houses, "
            "movable (Chara) signs, D4/D9 Vargas, and Rahu/Moon/Saturn influences."
        ),
        primary_houses=[12, 9, 3],
        secondary_houses=[7, 4, 10],
        required_vargas=["D1", "D9", "D4"],
        naisargika_karakas=["Rahu", "Moon", "Saturn", "Venus"],
        special_lagnas=["Udaya Lagna", "Arudha Lagna (AL)"],
        key_yogas_to_check=["VIPARITA_VIMALA_YOGA", "KALA_SARPA_DOSHA"],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="FOR_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 12th (Foreign residence), 9th (Long journeys), and 3rd/4th house connections.",
                classical_reference="BPHS Ch. 20 & Ch. 23",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="FOR_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D4 (residence shifts) and D9 placements of 12th and 9th lords in Chara (movable) signs.",
                classical_reference="BPHS Ch. 7",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="FOR_03",
                factor_category="KARAKA_STRENGTH",
                description="Check Rahu, Moon, and 12th lord influence on Lagna, 4th, or 10th houses.",
                classical_reference="Phaladeepika Ch. 14",
                weight=0.20,
            ),
            FrameworkChecklistRule(
                rule_code="FOR_04",
                factor_category="DASHA_AND_GOCHAR",
                description="Assess active Dasha of Rahu, 12th, or 9th lord and transits affecting the 4th/12th axis.",
                classical_reference="Phaladeepika Ch. 19 & 26",
                weight=0.25,
            ),
        ],
    ),
    AnalysisFrameworkDefinition(
        category_code="SPIRITUALITY_AND_DHARMA",
        title="Spirituality, Dharma, Life Purpose & Past-Life Merit",
        sanskrit_title="Dharma, Purva Punya & Moksha Vichara",
        description=(
            "Evaluates spiritual evolution, meditation, philosophical wisdom, Purva Punya (5th), Dharma (9th), and Moksha (12th) "
            "using D9 Navamsha, D20 Vimshamsha, D60 Shashtiamsha, and Jupiter/Ketu/Sun strength."
        ),
        primary_houses=[9, 12, 5],
        secondary_houses=[8, 4, 1],
        required_vargas=["D1", "D9", "D20", "D60"],
        naisargika_karakas=["Jupiter", "Ketu", "Sun", "Saturn"],
        special_lagnas=["Udaya Lagna", "Chandra Lagna"],
        key_yogas_to_check=[
            "HAMSA_YOGA",
            "VIPARITA_VIMALA_YOGA",
            "GAJAKESARI_YOGA",
            "KENDRA_TRIKONA_RAJA_YOGA",
        ],
        checklist_rules=[
            FrameworkChecklistRule(
                rule_code="SPIR_01",
                factor_category="HOUSE_AND_LORD",
                description="Inspect 9th (Dharma), 5th (Purva Punya), and 12th (Moksha) houses and their lords.",
                classical_reference="BPHS Ch. 16, 20 & 23",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="SPIR_02",
                factor_category="DIVISIONAL_VARGA",
                description="Examine D20 (Vimshamsha for spiritual sadhana), D9 (Navamsha), and D60 (Shashtiamsha).",
                classical_reference="BPHS Ch. 7",
                weight=0.30,
            ),
            FrameworkChecklistRule(
                rule_code="SPIR_03",
                factor_category="KARAKA_STRENGTH",
                description="Evaluate Atmakaraka / Sun, Moksha Karaka Ketu, and Guru Jupiter Shadbala & Vimshopaka Bala.",
                classical_reference="BPHS Ch. 27 & Ch. 32",
                weight=0.25,
            ),
            FrameworkChecklistRule(
                rule_code="SPIR_04",
                factor_category="DASHA_AND_GOCHAR",
                description="Check active Dasha of Ketu, Jupiter, or 5th/9th/12th lords and Jupiter transit over trinal houses.",
                classical_reference="Phaladeepika Ch. 20",
                weight=0.20,
            ),
        ],
    ),
]

CATEGORY_KEYWORDS: Dict[str, List[str]] = {
    "CAREER_AND_PROFESSION": [
        "career", "job", "profession", "promotion", "work", "business", "startup",
        "office", "boss", "authority", "leadership", "employment", "company", "role",
        "karma", "dashamsha", "d10", "corporate", "government", "ias", "service",
    ],
    "MARRIAGE_AND_RELATIONSHIPS": [
        "marriage", "marry", "spouse", "wife", "husband", "partner", "relationship",
        "love", "wedding", "divorce", "compatibility", "engagement", "dating",
        "kalatra", "navamsha", "d9", "upapada", "manglik", "kuja",
    ],
    "WEALTH_AND_FINANCE": [
        "wealth", "money", "finance", "financial", "income", "salary", "rich",
        "millionaire", "profit", "investment", "stock", "trading", "debt", "loan",
        "savings", "dhana", "labha", "lakshmi", "prosperity", "gains",
    ],
    "HEALTH_AND_LONGEVITY": [
        "health", "illness", "disease", "medical", "surgery", "hospital", "recovery",
        "vitality", "energy", "stress", "anxiety", "longevity", "pain", "immunity",
        "fitness", "ayur", "roga", "healing",
    ],
    "EDUCATION_AND_INTELLECT": [
        "education", "study", "studies", "exam", "degree", "university", "college",
        "phd", "masters", "research", "scholarship", "learning", "intelligence",
        "student", "academic", "vidya", "d24",
    ],
    "PROPERTY_AND_VEHICLES": [
        "property", "house", "home", "land", "real estate", "apartment", "flat",
        "buy", "construction", "vehicle", "car", "luxury", "conveyance", "bhumi",
        "vahana", "d4", "d16",
    ],
    "CHILDREN_AND_PROGENY": [
        "child", "children", "baby", "pregnancy", "conception", "son", "daughter",
        "progeny", "parenting", "fertility", "putra", "santana", "d7", "saptamsha",
    ],
    "FOREIGN_TRAVEL_AND_SETTLEMENT": [
        "foreign", "abroad", "overseas", "visa", "immigration", "settle", "relocate",
        "relocation", "international", "travel", "journey", "export", "import", "pr",
        "citizenship",
    ],
    "SPIRITUALITY_AND_DHARMA": [
        "spiritual", "spirituality", "meditation", "moksha", "dharma", "karma",
        "past life", "purpose", "soul", "guru", "mantra", "sadhana", "enlightenment",
        "ketu", "d20", "d60",
    ],
}


def classify_question_and_load_framework(
    request: QuestionClassificationRequest,
) -> QuestionClassificationResponse:
    """
    Deterministically classify a user's astrological question into primary and secondary
    Vedic consultation frameworks and return the complete classical evaluation blueprint.
    """
    raw_q = (request.question_text or "").strip()
    if not raw_q:
        raw_q = "What are my strongest career and financial periods?"

    lower_q = raw_q.lower()
    tokens = set(re.findall(r"[a-z0-9]+", lower_q))

    scores: List[Tuple[str, int, List[str]]] = []
    for cat_code, kw_list in CATEGORY_KEYWORDS.items():
        matched: List[str] = []
        pts = 0
        for kw in kw_list:
            if " " in kw:
                if kw in lower_q:
                    pts += 3
                    matched.append(kw)
            elif kw in tokens:
                pts += 2
                matched.append(kw)
            elif any(t.startswith(kw) for t in tokens if len(kw) >= 4):
                pts += 1
                matched.append(kw)
        scores.append((cat_code, pts, matched))

    scores.sort(key=lambda item: item[1], reverse=True)
    best_cat, best_pts, best_matched = scores[0]
    second_cat, second_pts, second_matched = scores[1]

    if best_pts == 0:
        primary_category = "CAREER_AND_PROFESSION"
        secondary_category: Optional[str] = "WEALTH_AND_FINANCE"
        confidence = 0.65
        matched_keywords = ["general", "life", "career"]
    else:
        primary_category = best_cat
        secondary_category = second_cat if second_pts > 0 else None
        confidence = round(min(0.98, 0.72 + (0.06 * best_pts)), 2)
        matched_keywords = list(dict.fromkeys(best_matched + second_matched))

    framework_by_code = {f.category_code: f for f in FRAMEWORK_CATALOG}
    active_fw = framework_by_code[primary_category]

    return QuestionClassificationResponse(
        framework_version=FRAMEWORK_VERSION,
        question_text=raw_q,
        primary_category=primary_category,
        secondary_category=secondary_category,
        confidence_score=confidence,
        matched_keywords=matched_keywords,
        active_framework=active_fw,
        all_frameworks=FRAMEWORK_CATALOG,
    )
