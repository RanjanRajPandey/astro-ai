# Astro-AI — Production-Ready AI-Powered Vedic Astrology / Kundli Platform

An enterprise-grade Vedic Astrology (Jyotish) computation, reasoning, and conversational AI platform built on a strict **Deterministic Calculation First** architecture.

## Core Principle

**The LLM is never the astrology calculation engine.**
1. **Python Astrology Engine (`astrology-engine`)**: Computes exact sidereal planetary positions, Ascendant & Bhavas, Nakshatras & Padas, 16 Shodashavarga Divisional Charts (D1–D60), 5-level Vimshottari Dashas (`Mahadasha` → `Prana`), Graha Drishti, Classical 6-fold Shadbala, Bhava Bala, Classical Yogas, and dynamic Gochara (Transits) using **Swiss Ephemeris**.
2. **Spring Boot Reasoning Core (`backend`)**: Classifies questions, executes declarative question analysis frameworks, builds a structured `EvidenceGraph` (`SUPPORTING | CHALLENGING | NEUTRAL`), computes candidate `TimeWindows`, and constructs a traceable `ReasoningChain`.
3. **AI Explainability Layer (`AIService`)**: Synthesizes natural-language answers strictly grounded in the calculated evidence and exposes every step via the interactive **"Why This Answer?"** inspector.

## Documentation

- [Master System Architecture](./ARCHITECTURE.md)
- [Astrological Methodology Specification](./docs/ASTROLOGY_SPECIFICATION.md)
- [Database Entity-Relationship Diagram](./docs/database-er.md)
