# Astro-AI: Production-Ready AI-Powered Vedic Astrology / Kundli Platform
## Master Technical Architecture & Specification (Phase 0)

---

## 1. Core Architectural Principle

**THE LLM MUST NEVER BE THE ASTROLOGY CALCULATION ENGINE.**

All astronomical positions, house cusps, nakshatras, divisional charts (D1–D60), 5-level Vimshottari Dashas, Graha/Rashi Drishti, Shadbala, Bhava Bala, Classical Yogas, and Gochara (Transits) are computed deterministically by the isolated **Astrology Calculation Engine**.

The **Evidence Engine** and **Reasoning Engine** evaluate explicit astrological rules over those deterministic outputs to produce a structured, traceable **Evidence Graph** and **Reasoning Chain**. The **AI Service (LLM)** only translates that verified structured data into natural language and invokes deterministic read-only tools for follow-up exploration.

```text
                         USER
                           │
                           ▼
                    React Frontend
                           │
                           ▼
              Spring Boot Backend API
                           │
                           ▼
                  Question Classifier
                           │
                           ▼
               Question Analysis Framework
                           │
                           ▼
         Python Astrology Calculation Engine (Swiss Ephemeris)
                           │
             ┌─────────────┼─────────────┐
             │             │             │
             ▼             ▼             ▼
          Charts         Dashas       Transits
             │             │             │
             ├─────────────┼─────────────┤
             │             │             │
             ▼             ▼             ▼
         Drishti       Strengths       Yogas
             │             │             │
             └─────────────┼─────────────┘
                           ▼
                     Evidence Engine
                           │
                           ▼
                    Reasoning Engine
                           │
                           ▼
                       LLM / AI
                           │
                           ▼
                 Natural Language Answer
```

---

## 2. System Architecture Diagram

```mermaid
flowchart TD
    subgraph Client_Layer ["Client Layer (Web & Future Mobile)"]
        UI["React + TypeScript SPA<br/>(Kundli Charts, Dasha Explorer, AI Chat, 'Why This Answer?' Inspector)"]
    end

    subgraph Spring_Backend ["Core Backend & Reasoning Layer (Java 21 / Spring Boot 3)"]
        API["REST API & SSE Gateway<br/>(Spring Security + JWT + Bucket4j Rate Limiter)"]
        BPE["Birth Profile & Historical Timezone Engine"]
        QC["Question Classifier"]
        QAF["Question Analysis Framework Registry"]
        
        subgraph Cognitive_Core ["Deterministic Explainability Pipeline"]
            EE["Evidence Engine<br/>(Supporting / Challenging / Neutral Factors)"]
            TAE["Temporal Analysis Engine<br/>(Dasha × Transit Window Finder)"]
            RE["Reasoning Engine<br/>(Traceable Rule-Based DAG)"]
        end
        
        AIS["AI Service Abstraction & Tool Layer<br/>(Hallucination Guardrails + Context Manager)"]
    end

    subgraph Python_Astro_Engine ["Deterministic Astrology Engine (Python 3.12 / FastAPI)"]
        SWE["Swiss Ephemeris Core (pyswisseph)<br/>Sidereal / Ayanamsha / Julian Day Engine"]
        
        subgraph Calculators ["Vedic Calculation Modules"]
            PC["Planetary & Nakshatra Engine"]
            HC["Ascendant & House Engine"]
            DC["Divisional Chart Engine (D1-D60)"]
            VDE["5-Level Vimshottari Dasha Engine"]
            DRE["Drishti (Aspect) Engine"]
            PSE["Planetary (Shadbala) & House Strength Engine"]
            YE["Classical Yoga Engine"]
            TE["Dynamic Transit (Gochara) Engine"]
        end
    end

    subgraph Data_Layer ["Data & External Services"]
        PG[("PostgreSQL 16<br/>(Relational + Evidence Graphs)")]
        CACHE[("Redis / Caffeine Cache<br/>(Verified Chart & Transit Cache)")]
        GEO["Geocoding & IANA Timezone Service"]
        LLM["LLM Provider API<br/>(OpenAI / Anthropic / Gemini / Local)"]
    end

    UI <-->|HTTPS REST / SSE| API
    API --> BPE
    BPE --> GEO
    BPE --> PG
    API --> QC
    QC --> QAF
    QAF --> EE
    
    BPE <-->|Internal REST / JSON| SWE
    EE <-->|Fetch Natal / Dasha / Transit| CACHE
    CACHE <-->|Cache Miss| SWE
    SWE --> PC & HC & DC & VDE & DRE & PSE & YE & TE
    
    EE --> TAE
    TAE --> RE
    RE --> PG
    RE -->|Structured Evidence & Reasoning JSON| AIS
    AIS <-->|Read-Only Deterministic Tool Calls| EE
    AIS <-->|Prompt + Structured Context| LLM
    AIS -->|Streamed Natural Language + Reasoning Trace| UI
```

---

## 3. Monorepo Structure

```text
astro-ai/
├── frontend/                  # React 18 + TypeScript + Vite + Tailwind CSS
├── backend/                   # Java 21 + Spring Boot 3 + Spring Security + JPA
├── astrology-engine/          # Python 3.12 + FastAPI + Swiss Ephemeris (pyswisseph)
├── ai-service/                # Prompt templates, tool schemas, AI evaluation suite
├── database/                  # PostgreSQL initialization & backup scripts
├── docs/                      # ASTROLOGY_SPECIFICATION.md, database-er.md, API docs
├── tests/                     # E2E integration & golden chart validation suites
├── docker/                    # Multi-stage Dockerfiles & Nginx configuration
├── scripts/                   # Development & verification automation scripts
├── .env.example
├── docker-compose.yml
├── README.md
├── ARCHITECTURE.md
└── CONTRIBUTING.md
```

---

## 4. Module Dependency Graph

```mermaid
flowchart BT
    subgraph L1 ["Layer 1: Astronomical & Geodetic Foundation"]
        GEO["Coordinates & Historical Timezone"]
        EPH["Swiss Ephemeris & Ayanamsha Engine"]
        GEO --> EPH
    end

    subgraph L2 ["Layer 2: Primary Natal Primitives"]
        PLN["Planetary Positions & Dignities"]
        ASC["Ascendant (Lagna) & 12 Houses"]
        NAK["Nakshatra & Pada Engine"]
        EPH --> PLN
        EPH --> ASC
        PLN --> NAK
        ASC --> PLN
    end

    subgraph L3 ["Layer 3: Derived Astrological Systems"]
        DIV["Divisional Charts (D1-D60)"]
        DRS["Drishti (Aspects) Engine"]
        DSH["5-Level Vimshottari Dasha Engine"]
        SHD["Shadbala (Planetary Strength) Engine"]
        BHV["Bhava Bala (House Strength) Engine"]
        YOG["Classical Yoga Engine"]
        TRN["Transit (Gochara) Engine"]

        PLN & ASC --> DIV
        PLN & ASC --> DRS
        NAK --> DSH
        PLN & ASC & DIV & DRS --> SHD
        SHD & ASC & DRS --> BHV
        PLN & ASC & DIV & DRS & SHD --> YOG
        EPH & PLN & ASC --> TRN
    end

    subgraph L4 ["Layer 4: Analytical & Reasoning Core"]
        QCL["Question Classifier & Framework Loader"]
        TMP["Temporal Analysis Engine"]
        EVD["Evidence Engine"]
        RSN["Reasoning Engine"]

        DIV & DRS & DSH & SHD & BHV & YOG & TRN --> EVD
        DSH & TRN & EVD --> TMP
        QCL --> EVD
        EVD & TMP --> RSN
    end

    subgraph L5 ["Layer 5: AI & Presentation"]
        AIT["AI Tool Layer & AIService"]
        API_UI["REST API & React Frontend"]
        RSN --> AIT
        AIT --> API_UI
        EVD & RSN --> API_UI
    end
```

---

## 5. Development Phases Roadmap

- **Phase 0**: Requirements, Architecture, and Astrological Specification Review
- **Phase 1**: Repository setup and development environment
- **Phase 2**: Birth profile and location/timezone engine
- **Phase 3**: Astronomical calculation engine
- **Phase 4**: Ascendant + house engine
- **Phase 5**: D1 chart engine and visualization
- **Phase 6**: Nakshatra engine
- **Phase 7**: 5-Level Vimshottari Dasha engine
- **Phase 8**: Divisional charts (D1–D60 Shodashavarga)
- **Phase 9**: Drishti (Aspects) engine
- **Phase 10**: Planetary strength (Shadbala) engine
- **Phase 11**: House strength (Bhava Bala) engine
- **Phase 12**: Classical Yoga engine
- **Phase 13**: Transit (Gochara) engine
- **Phase 14**: Temporal analysis engine
- **Phase 15**: Question classifier and analysis frameworks
- **Phase 16**: Evidence engine
- **Phase 17**: Reasoning engine
- **Phase 18**: AI tool layer and provider abstraction
- **Phase 19**: AI chatbot and "Why This Answer?" explainability UI
- **Phase 20**: Authentication and authorization
- **Phase 21**: Saved profiles and conversation history
- **Phase 22**: Unit, integration, golden reference, and AI evaluation testing
- **Phase 23**: Security hardening and privacy controls
- **Phase 24**: Performance and caching optimization
- **Phase 25**: Production deployment configuration
- **Phase 26**: Observability, monitoring, and maintenance
