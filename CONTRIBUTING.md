# Contributing to Astro-AI

## Architectural Golden Rules

1. **The LLM is NEVER the calculation engine**:
   - All planetary longitudes, houses, nakshatras, divisional charts (D1–D60), Vimshottari Dashas, Drishti, Shadbala, Bhava Bala, Yogas, and Gochara (Transits) must be computed deterministically in `astrology-engine/`.
   - Any PR that asks an LLM to calculate or guess astronomical positions, dasha dates, or yoga presence will be rejected.

2. **Astrological Rule Fidelity**:
   - Every astrological calculation and rule evaluator must trace directly to `docs/ASTROLOGY_SPECIFICATION.md`.
   - Do not silently mix traditions (e.g., Parashari vs. Jaimini vs. KP) without explicit configuration flags and specification documentation.

3. **Mandatory Traceability ("Why This Answer?")**:
   - Every `EvidenceItem` emitted by `EvidenceEngine` must include `factor`, `category`, `observation`, `rule`, `effect`, `classification` (`SUPPORTING | CHALLENGING | NEUTRAL`), `importance`, and `source`.
   - Never suppress or hide contradictory astrological indicators.

4. **Phase-by-Phase Validation**:
   - Every module must include unit tests and golden reference validation tests before integration.
   - Never log raw user PII (birth dates, coordinates, full names, or private chat transcripts).
