# Vedic Astrology Calculation Specification (`ASTROLOGY_SPECIFICATION.md`)

> **Status**: Phase 0 Baseline Specification (Configurable Defaults Documented)
> **Rule Integrity Policy**: No astrological rule may be silently mixed across traditions or invented by an LLM. Every calculation in `astrology-engine` and rule evaluator in `backend` must trace to a section in this specification.

---

## 1. Zodiac & Coordinate System
- **Zodiac Type**: **Sidereal (Nirayana)**
- **Reference Coordinate Frame**: Geocentric ecliptic coordinates computed via **Swiss Ephemeris (`pyswisseph`)** using high-precision `.se1` ephemeris data files.
- **Default Ayanamsha**: **Lahiri / Chitrapaksha (`SE_SIDM_LAHIRI`)** (Indian Rashtriya Panchang standard).
  - *Supported Configurable Alternatives*: True Chitrapaksha (`SE_SIDM_TRUE_CITRA`), Krishnamurti (`SE_SIDM_KRISHNAMURTI`), B.V. Raman (`SE_SIDM_RAMAN`).

## 2. Grahas (Planetary Bodies) & Lunar Nodes
- **Nine Grahas**: Sun (*Surya*), Moon (*Chandra*), Mars (*Mangala*), Mercury (*Budha*), Jupiter (*Guru*), Venus (*Shukra*), Saturn (*Shani*), Rahu (North Node), Ketu (South Node).
- **Rahu / Ketu Node Calculation**:
  - **Default**: **Mean Node (`SE_MEAN_NODE`)** (Classical Parashari default; always retrograde).
  - **Configurable Alternative**: **True Node (`SE_TRUE_NODE`)**.
  - **Ketu Invariant**: $\lambda_{\text{Ketu}} = (\lambda_{\text{Rahu}} + 180^\circ) \bmod 360^\circ$.

## 3. House (Bhava) System
- **Primary Rashi/Sign House System**: **Whole Sign Houses (*Rashi = Bhava*)**
  - The sign containing the Ascendant (*Udaya Lagna*) degree is House 1; subsequent signs in zodiacal order are Houses 2 through 12.
  - Used for House Lordships (*Bhavadhipati*), Parashari Rashi/Graha Drishti, and Classical Yoga detection.
- **Cuspal / Chalit System**: **Sripati Bhava Chalit (Porphyry Trisection)**
  - Computes exact Ascendant (*Lagna*) cusp, 10th house (*Madhya Lagna / MC*) cusp, and trisects the quadrants to determine *Bhava Madhya* (house midpoints) and *Bhava Sandhi* (house junctions), used in **Bhava Bala** and cuspal strength grading.

## 4. Nakshatra & Pada System
- **27 Equal Nakshatras**: Each spanning $13^\circ 20'$ ($800$ arcminutes) of the $360^\circ$ sidereal zodiac, starting from $0^\circ 00'$ Aries (*Ashwini*).
- **4 Padas (Quarters) per Nakshatra**: Each spanning $3^\circ 20'$ ($200$ arcminutes), corresponding directly to the **D9 (Navamsha)** sign progression.
- **Vimshottari Nakshatra Lords (Repeating 3× across 27 Nakshatras)**:
  1. Ketu (*Ashwini, Magha, Moola*)
  2. Venus (*Bharani, Purva Phalguni, Purva Ashadha*)
  3. Sun (*Krittika, Uttara Phalguni, Uttara Ashadha*)
  4. Moon (*Rohini, Hasta, Shravana*)
  5. Mars (*Mrigashira, Chitra, Dhanishta*)
  6. Rahu (*Ardra, Swati, Shatabhisha*)
  7. Jupiter (*Punarvasu, Vishakha, Purva Bhadrapada*)
  8. Saturn (*Pushya, Anuradha, Uttara Bhadrapada*)
  9. Mercury (*Ashlesha, Jyeshtha, Revati*)

## 5. Planetary Dignities, Combustion & Relationships
- **Exaltation (*Uchcha*), Deep Exaltation Degree, Moolatrikona, Own Sign (*Swakshetra*), and Debilitation (*Neecha*)**:
  | Graha | Exaltation Sign (Deep Degree) | Debilitation Sign | Moolatrikona Sign (Range) | Own Sign(s) |
  | :--- | :--- | :--- | :--- | :--- |
  | **Sun** | Aries ($10^\circ$) | Libra ($10^\circ$) | Leo ($0^\circ\text{–}20^\circ$) | Leo |
  | **Moon** | Taurus ($3^\circ$) | Scorpio ($3^\circ$) | Taurus ($3^\circ\text{–}30^\circ$) | Cancer |
  | **Mars** | Capricorn ($28^\circ$) | Cancer ($28^\circ$) | Aries ($0^\circ\text{–}12^\circ$) | Aries, Scorpio |
  | **Mercury** | Virgo ($15^\circ$) | Pisces ($15^\circ$) | Virgo ($15^\circ\text{–}20^\circ$) | Gemini, Virgo |
  | **Jupiter** | Cancer ($5^\circ$) | Capricorn ($5^\circ$) | Sagittarius ($0^\circ\text{–}10^\circ$) | Sagittarius, Pisces |
  | **Venus** | Pisces ($27^\circ$) | Virgo ($27^\circ$) | Libra ($0^\circ\text{–}15^\circ$) | Taurus, Libra |
  | **Saturn** | Libra ($20^\circ$) | Aries ($20^\circ$) | Aquarius ($0^\circ\text{–}20^\circ$) | Capricorn, Aquarius |
  | **Rahu** | Taurus | Scorpio | Aquarius (Co-lord convention) | Aquarius |
  | **Ketu** | Scorpio | Taurus | Scorpio (Co-lord convention) | Scorpio |
- **Combustion (*Asta*) Orbs from Sun**:
  - Moon: $12^\circ$ | Mars: $17^\circ$ | Mercury: $14^\circ$ ($12^\circ$ if retrograde) | Jupiter: $11^\circ$ | Venus: $10^\circ$ ($8^\circ$ if retrograde) | Saturn: $15^\circ$. (Rahu/Ketu are never combust).
- **Panchadha Maitri (5-Fold Friendship)**:
  - Combines *Naisargika Maitri* (Natural Relationship) + *Tatkalika Maitri* (Temporal Relationship: planets placed in 2nd, 3rd, 4th, 10th, 11th, 12th signs from a reference planet are Temporal Friends; others are Temporal Enemies) into: `ADHI_MITRA`, `MITRA`, `SAMA`, `SHATRU`, `ADHI_SHATRU`.

## 6. Shodashavarga — 16 Divisional Charts (BPHS Standard)
All 16 divisional charts follow classical *Brihat Parashara Hora Shastra* (BPHS Ch. 6) rules:
- **D1 (Rashi)**, **D2 (Parashari Hora)**, **D3 (Parashari Drekkana)**, **D4 (Chaturthamsha)**, **D7 (Saptamsha)**, **D9 (Navamsha)**, **D10 (Dashamsha)**, **D12 (Dwadashamsha)**, **D16 (Shodashamsha)**, **D20 (Vimshamsha)**, **D24 (Chaturvimshamsha/Siddhamsha)**, **D27 (Saptavimshamsha/Bhamsha)**, **D30 (Parashari Trimshamsha)**, **D40 (Khavedamsha)**, **D45 (Akshavedamsha)**, **D60 (Shashtiamsha)**.

## 7. 5-Level Vimshottari Dasha System
- **Hierarchy**: `Mahadasha (L1)` → `Antardasha (L2)` → `Pratyantardasha (L3)` → `Sookshma (L4)` → `Prana (L5)`.
- **Mahadasha Years ($120\text{ Total}$)**: Ketu $7$, Venus $20$, Sun $6$, Moon $10$, Mars $7$, Rahu $18$, Jupiter $16$, Saturn $19$, Mercury $17$.
- **Default Year Length**: **$365.2425\text{ days}$** (Gregorian solar year alignment; configurable to $365.256363$ sidereal days or $360$ Savana days).
- **Sub-Period Recursive Proportion**: Each sub-cycle starts with the lord of the parent period and progresses in the canonical 9-planet Vimshottari order with duration $= \text{ParentDuration} \times (\text{PlanetYears} / 120)$.

## 8. Graha Drishti (Planetary Aspects)
- **Universal Aspect**: Every planet casts a full ($100\%$ / $60$ Virupa) aspect on the **7th house/sign** from its placement.
- **Special Full Aspects (*Vishesha Drishti*)**:
  - **Mars**: **4th** and **8th** houses/signs from placement.
  - **Jupiter**: **5th** and **9th** houses/signs from placement.
  - **Saturn**: **3rd** and **10th** houses/signs from placement.
  - **Rahu / Ketu**: 7th house aspect by default; optional configurable flag for 5th & 9th nodal aspects.
- **Longitudinal Aspect Strength (*Sphuta Drishti*)**: Computed in Virupas ($0\text{–}60$) per BPHS Ch. 26 for Shadbala *Drik Bala*.

## 9. Planetary Strength (Shadbala) & House Strength (Bhava Bala)
- **Classical Shadbala** (6-Fold Strength in Virupas & Rupas, $1\text{ Rupa} = 60\text{ Virupas}$):
  1. *Sthana Bala* (Positional)
  2. *Dig Bala* (Directional)
  3. *Kala Bala* (Temporal)
  4. *Chesta Bala* (Motional)
  5. *Naisargika Bala* (Natural)
  6. *Drik Bala* (Aspectual)
  - Minimum required Rupas per BPHS: Sun ($6.5$), Moon ($6.0$), Mars ($5.0$), Mercury ($7.0$), Jupiter ($6.5$), Venus ($5.5$), Saturn ($5.0$).
- **Classical Bhava Bala**: Sum of *Bhavadhipati Bala* (Lord's Shadbala), *Bhava Dig Bala*, and *Bhava Drishti Bala*, supplemented by transparent occupant benefic/malefic modifiers.
