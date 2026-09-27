export type ZodiacSign =
  | 'Aries'
  | 'Taurus'
  | 'Gemini'
  | 'Cancer'
  | 'Leo'
  | 'Virgo'
  | 'Libra'
  | 'Scorpio'
  | 'Sagittarius'
  | 'Capricorn'
  | 'Aquarius'
  | 'Pisces';

export type GrahaName =
  | 'Sun'
  | 'Moon'
  | 'Mars'
  | 'Mercury'
  | 'Jupiter'
  | 'Venus'
  | 'Saturn'
  | 'Rahu'
  | 'Ketu';

export type EvidenceClassification = 'SUPPORTING' | 'CHALLENGING' | 'NEUTRAL';
export type EvidenceImportance = 'LOW' | 'MEDIUM' | 'HIGH';
export type EvidenceSource =
  | 'NATAL'
  | 'DASHA'
  | 'TRANSIT'
  | 'DIVISIONAL'
  | 'DRISHTI'
  | 'STRENGTH'
  | 'YOGA';

export interface EvidenceItem {
  factor: string;
  category: string;
  observation: string;
  rule: string;
  effect: string;
  classification: EvidenceClassification;
  importance: EvidenceImportance;
  source: EvidenceSource;
}

export interface GazetteerCity {
  name: string;
  stateOrRegion: string;
  countryCode: string;
  latitude: number;
  longitude: number;
  timezoneId: string;
}

export interface BirthProfile {
  id: string;
  userId: string;
  name: string;
  dateOfBirth: string;
  timeOfBirth: string | null;
  birthTimeAccurate: boolean;
  placeOfBirth: string;
  gender: string;
  latitude: number;
  longitude: number;
  timezone: string;
  utcOffsetHours: number;
  utcBirthTime: string;
  calculationWarnings: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateBirthProfilePayload {
  name: string;
  dateOfBirth: string;
  timeOfBirth?: string | null;
  placeOfBirth: string;
  gender: string;
  latitude?: number;
  longitude?: number;
  timezone?: string;
}

export interface PlanetaryRelationshipDetail {
  natural: string;
  temporal: string;
  compound: string;
}

export interface PlanetPosition {
  planet: GrahaName;
  longitude: number;
  latitude: number;
  speedLongitude: number;
  sign: ZodiacSign;
  sanskritSign: string;
  signIndex: number;
  signLord: GrahaName;
  degreeInSign: number;
  degreeDms: string;
  house: number;
  nakshatra: string;
  nakshatraIndex: number;
  nakshatraLord: GrahaName;
  pada: number;
  retrograde: boolean;
  combust: boolean;
  angularDistanceFromSun: number | null;
  exalted: boolean;
  debilitated: boolean;
  moolatrikona: boolean;
  ownSign: boolean;
  dignity: string;
  dispositorRelationship: string;
  planetaryRelationships: Record<string, PlanetaryRelationshipDetail>;
}

export interface AscendantSummary {
  longitude: number;
  sign: ZodiacSign;
  sanskritSign: string;
  signIndex: number;
  degreeInSign: number;
  degreeDms: string;
  nakshatra: string;
  nakshatraIndex: number;
  nakshatraLord: GrahaName;
  pada: number;
  lagnaLord: GrahaName;
  lagnaLordSign: ZodiacSign;
  lagnaLordHouse: number;
  lagnaLordDignity: string;
  chandraLagnaSign: ZodiacSign;
  suryaLagnaSign: ZodiacSign;
  arudhaLagnaSign: ZodiacSign;
  arudhaLagnaHouse: number;
  upapadaLagnaSign: ZodiacSign;
  upapadaLagnaHouse: number;
}

export interface HouseAspect {
  sourcePlanet: GrahaName;
  sourceHouse: number;
  sourceSign: ZodiacSign;
  aspectHouseDistance: number;
  aspectType: string;
  rule: string;
}

export interface HouseDetail {
  houseNumber: number;
  sign: ZodiacSign;
  sanskritSign: string;
  signIndex: number;
  lordPlanet: GrahaName;
  lordPlacedInHouse: number;
  lordPlacedInSign: ZodiacSign;
  lordDignity: string;
  degreeCusp: number;
  sripatiCuspLongitude: number;
  sripatiStartLongitude: number;
  sripatiEndLongitude: number;
  occupants: GrahaName[];
  chalitOccupants: GrahaName[];
  aspectsReceived: HouseAspect[];
  purushartha: 'DHARMA' | 'ARTHA' | 'KAMA' | 'MOKSHA';
  classifications: string[];
  significations: string[];
  baselineStrengthScore: number;
  strengthGrade: 'STRONG' | 'MODERATE' | 'CHALLENGED';
}

export interface KundliChartResponse {
  chartId: string;
  vargaCode: string;
  vargaName: string;
  birthProfile: BirthProfile;
  ayanamshaType: string;
  ayanamshaValue: number;
  houseSystem: string;
  nodeType: string;
  ascendant: AscendantSummary;
  moonSign: ZodiacSign;
  moonSanskritSign: string;
  moonNakshatra: string;
  moonPada: number;
  sunSign: ZodiacSign;
  planets: PlanetPosition[];
  houses: HouseDetail[];
}

export interface NakshatraPlacementItem {
  bodyName: string;
  longitude: number;
  rashiSign: ZodiacSign;
  nakshatraName: string;
  nakshatraIndex: number;
  pada: number;
  padaNavamshaSign: ZodiacSign;
  rulerPlanet: GrahaName;
  degreeInNakshatra: number;
  degreeInNakshatraDms: string;
  elapsedFraction: number;
  remainingFraction: number;
  deity: string;
  gana: string;
  nadi: string;
  yoni: string;
  symbol: string;
  taraNumberFromMoon: number;
  taraNameFromMoon: string;
  taraQuality: 'FAVORABLE' | 'CHALLENGING' | 'MIXED_INTENSE';
  relationshipToNakshatraLord: string;
}

export interface NakshatraCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ayanamshaType: string;
  ayanamshaValue: number;
  janmaNakshatra: string;
  janmaNakshatraIndex: number;
  janmaPada: number;
  janmaNakshatraLord: GrahaName;
  janmaRashi: ZodiacSign;
  moonElapsedFraction: number;
  moonRemainingFraction: number;
  placements: NakshatraPlacementItem[];
}

export interface DashaPeriodNode {
  planet: GrahaName;
  level: number;
  levelName: string;
  startDateTime: string;
  endDateTime: string;
  unclampedStartDateTime: string;
  durationDays: number;
  durationYears: number;
  isCurrentlyActive: boolean;
  isBirthBalancePeriod: boolean;
  subPeriods: DashaPeriodNode[];
}

export interface ActiveDashaStackItem {
  level: number;
  levelName: string;
  planet: GrahaName;
  startDateTime: string;
  endDateTime: string;
  unclampedStartDateTime: string;
  durationDays: number;
  elapsedPercentage: number;
}

export interface DashaCalculationResponse {
  birthProfileId: string;
  birthUtcDatetimeIso: string;
  targetUtcDatetimeIso: string;
  julianDayUt: number;
  ayanamshaType: string;
  ayanamshaValue: number;
  yearLengthDays: number;
  moonLongitude: number;
  janmaNakshatra: string;
  janmaPada: number;
  birthDashaLord: GrahaName;
  moonElapsedFraction: number;
  moonRemainingFraction: number;
  birthBalanceYears: number;
  birthBalanceDays: number;
  birthBalanceFormatted: string;
  activeStack: ActiveDashaStackItem[];
  activeSookshmaPeriods: DashaPeriodNode[];
  activePranaPeriods: DashaPeriodNode[];
  mahadashas: DashaPeriodNode[];
}

export interface DivisionalPlanetPlacement {
  planet: GrahaName;
  d1Longitude: number;
  d1Sign: ZodiacSign;
  d1House: number;
  vargaSign: ZodiacSign;
  vargaSanskritSign: string;
  vargaSignIndex: number;
  vargaSignLord: GrahaName;
  vargaHouse: number;
  partNumber: number;
  isVargottama: boolean;
  dignityInVarga: string;
  retrograde: boolean;
  combust: boolean;
  shashtiamshaName?: string | null;
  shashtiamshaQuality?: 'BENEFIC' | 'MALEFIC' | null;
}

export interface DivisionalHouseSummary {
  houseNumber: number;
  sign: ZodiacSign;
  sanskritSign: string;
  signIndex: number;
  lordPlanet: GrahaName;
  occupants: GrahaName[];
}

export interface DivisionalChartData {
  vargaCode: string;
  divisionNumber: number;
  sanskritName: string;
  title: string;
  domainSignification: string;
  ascendantSign: ZodiacSign;
  ascendantSanskritSign: string;
  ascendantSignIndex: number;
  ascendantLord: GrahaName;
  isAscendantVargottama: boolean;
  vargottamaPlanets: string[];
  planets: DivisionalPlanetPlacement[];
  houses: DivisionalHouseSummary[];
}

export interface DivisionalCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ayanamshaType: string;
  ayanamshaValue: number;
  d1AscendantSign: ZodiacSign;
  d9VargottamaSummary: string[];
  charts: DivisionalChartData[];
}



