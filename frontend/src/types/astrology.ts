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
  rule?: string;
  ruleReference?: string;
  effect?: string;
  effectDescription?: string;
  classification?: EvidenceClassification | 'FAVORABLE';
  finding?: 'FAVORABLE' | 'CHALLENGING' | 'NEUTRAL';
  importance?: EvidenceImportance;
  source?: EvidenceSource;
  weight?: number;
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

export interface HouseAspectEntry {
  sourcePlanet: GrahaName;
  sourceSign: ZodiacSign;
  sourceHouse: number;
  targetHouse: number;
  targetSign: ZodiacSign;
  houseOffset: number;
  aspectType: string;
  isFullAspect: boolean;
  isSpecialAspect: boolean;
  padaFraction: string;
  virupaStrength: number;
  aspectNature: 'BENEFIC' | 'MALEFIC';
  ruleApplied: string;
}

export interface PlanetToPlanetAspect {
  sourcePlanet: GrahaName;
  sourceHouse: number;
  sourceSign: ZodiacSign;
  sourceLongitude: number;
  targetPlanet: GrahaName;
  targetHouse: number;
  targetSign: ZodiacSign;
  targetLongitude: number;
  houseOffset: number;
  angularSeparationDeg: number;
  orbFromExactAspectDeg: number;
  aspectType: string;
  isFullAspect: boolean;
  isSpecialAspect: boolean;
  padaFraction: string;
  virupaStrength: number;
  sphutaVirupaStrength: number;
  aspectNature: 'BENEFIC' | 'MALEFIC';
  ruleApplied: string;
}

export interface MutualAspectSummary {
  planetA: GrahaName;
  houseA: number;
  signA: ZodiacSign;
  planetB: GrahaName;
  houseB: number;
  signB: ZodiacSign;
  relationshipType: 'CONJUNCTION_YUTI' | 'MUTUAL_7TH_OPPOSITION' | 'MUTUAL_SPECIAL_LOCK';
  aToBAspectType: string;
  bToAAspectType: string;
  combinedVirupaStrength: number;
  exactOrbDeg: number;
  description: string;
}

export interface AspectCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ascendantSign: ZodiacSign;
  ayanamshaType: string;
  rahuKetuTrinalAspects: boolean;
  houseAspects: HouseAspectEntry[];
  planetAspects: PlanetToPlanetAspect[];
  mutualRelationships: MutualAspectSummary[];
}

export interface SthanaBalaBreakdown {
  uchchaBala: number;
  saptavargajaBala: number;
  ojhayugmarasyamsaBala: number;
  kendradiBala: number;
  drekkanaBala: number;
  total: number;
}

export interface KalaBalaBreakdown {
  nathonnathaBala: number;
  pakshaBala: number;
  tribhagaBala: number;
  varaBala: number;
  ayanaBala: number;
  total: number;
}

export interface PlanetStrengthEntry {
  planet: GrahaName;
  sign: ZodiacSign;
  house: number;
  d1Dignity: string;
  isRetrograde: boolean;
  sthanaBala: number;
  sthanaBreakdown: SthanaBalaBreakdown;
  digBala: number;
  kalaBala: number;
  kalaBreakdown: KalaBalaBreakdown;
  chestaBala: number;
  naisargikaBala: number;
  drikBala: number;
  totalShadbalaVirupas: number;
  totalShadbalaRupas: number;
  requiredMinimumRupas: number;
  shadbalaRatio: number;
  vimshopakaBala: number;
  vimshopakaPercentage: number;
  strengthGrade: 'VERY_STRONG' | 'ADEQUATE' | 'MODERATE' | 'WEAK';
  rank: number;
}

export interface ShadbalaCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ascendantSign: ZodiacSign;
  ayanamshaType: string;
  strongestPlanet: GrahaName;
  weakestPlanet: GrahaName;
  planets: PlanetStrengthEntry[];
}

export interface HouseStrengthEntry {
  houseNumber: number;
  sign: ZodiacSign;
  sanskritSign: string;
  lordPlanet: GrahaName;
  signNature: 'NARA_BIPED' | 'JALACHARA_WATER' | 'KEETA_INSECT' | 'CHATUSHPADA_QUADRUPED';
  purushartha: 'DHARMA' | 'ARTHA' | 'KAMA' | 'MOKSHA';
  domainTitle: string;
  occupants: GrahaName[];
  bhavadhipatiBala: number;
  bhavaDigBala: number;
  bhavaDrishtiBala: number;
  occupantFactor: number;
  totalBhavaBalaVirupas: number;
  totalBhavaBalaRupas: number;
  strengthGrade: 'VERY_STRONG' | 'STRONG' | 'MODERATE' | 'WEAK';
  rank: number;
}

export interface PurusharthaSummary {
  purushartha: 'DHARMA' | 'ARTHA' | 'KAMA' | 'MOKSHA';
  houses: number[];
  averageRupas: number;
  dominantHouse: number;
}

export interface BhavaBalaCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ascendantSign: ZodiacSign;
  ayanamshaType: string;
  strongestHouse: number;
  weakestHouse: number;
  averageRupas: number;
  purusharthaSummaries: PurusharthaSummary[];
  houses: HouseStrengthEntry[];
}

export type YogaCategory =
  | 'PANCHA_MAHAPURUSHA'
  | 'LUNAR_YOGA'
  | 'SOLAR_YOGA'
  | 'RAJA_YOGA'
  | 'DHANA_YOGA'
  | 'VIPARITA_RAJA_YOGA'
  | 'SPECIAL_YOGA'
  | 'NEECHA_BHANGA'
  | 'DOSHA';

export type YogaStatus = 'ACTIVE' | 'CANCELLED_OR_MITIGATED' | 'NOT_FORMED' | 'ABSENT';
export type YogaStrength = 'VERY_STRONG' | 'STRONG' | 'MODERATE' | 'MILD' | 'MITIGATED' | 'INACTIVE' | 'NONE';

export interface YogaEvaluationEntry {
  yogaCode: string;
  name: string;
  sanskritName: string;
  category: YogaCategory;
  definition: string;
  classicalEffect: string;
  requiredConditions: string[];
  detectedConditions: string[];
  planetsInvolved: GrahaName[];
  housesInvolved: number[];
  status: YogaStatus;
  strength: YogaStrength;
  isBenefic: boolean;
}

export interface YogaCalculationResponse {
  birthProfileId: string;
  chartId: string;
  utcDatetimeIso: string;
  julianDayUt: number;
  ascendantSign: ZodiacSign;
  moonSign: ZodiacSign;
  ayanamshaType: string;
  activeYogaCount: number;
  activeDoshaCount: number;
  mitigatedCount: number;
  totalEvaluatedCount: number;
  activeYogas: YogaEvaluationEntry[];
  allEvaluatedYogas: YogaEvaluationEntry[];
}

export interface TransitPlanetEntry {
  planet: GrahaName;
  natalSign: ZodiacSign;
  natalHouseFromLagna: number;
  transitLongitude: number;
  transitSign: ZodiacSign;
  transitSanskritSign: string;
  transitDegreeDms: string;
  transitNakshatra: string;
  transitPada: number;
  isRetrograde: boolean;
  houseFromMoon: number;
  houseFromLagna: number;
  isBeneficFromMoon: boolean;
  vedhaObstructed: boolean;
  vedhaObstructor: GrahaName | null;
  gocharStatus: 'FAVORABLE' | 'VEDHA_OBSTRUCTED' | 'NEUTRAL_OR_CHALLENGING';
  taraBalaCategory: string;
  isTaraFavorable: boolean;
  classicalSummary: string;
}

export interface SadeSatiStatus {
  sadeSatiActive: boolean;
  dhaiyaActive: boolean;
  phase:
    | 'RISING_12TH'
    | 'PEAK_JANMA_1ST'
    | 'SETTING_2ND'
    | 'DHAIYA_KANTAKA_4TH'
    | 'DHAIYA_ASHTAMA_8TH'
    | 'NONE';
  saturnTransitSign: ZodiacSign;
  saturnHouseFromMoon: number;
  saturnHouseFromLagna: number;
  description: string;
}

export interface DoubleTransitHouseEntry {
  houseNumber: number;
  sign: ZodiacSign;
  sanskritSign: string;
  domainTitle: string;
  jupiterInfluence: string | null;
  saturnInfluence: string | null;
  isActivated: boolean;
}

export interface TransitCalculationResponse {
  birthProfileId: string;
  natalUtcDatetimeIso: string;
  transitUtcDatetimeIso: string;
  transitJulianDayUt: number;
  natalAscendantSign: ZodiacSign;
  natalMoonSign: ZodiacSign;
  natalMoonNakshatra: string;
  ayanamshaType: string;
  favorableTransitCount: number;
  vedhaObstructedCount: number;
  sadeSati: SadeSatiStatus;
  doubleTransitHouses: DoubleTransitHouseEntry[];
  planets: TransitPlanetEntry[];
}

export type WindowClassification =
  | 'HIGH_OPPORTUNITY'
  | 'FAVORABLE_GROWTH'
  | 'STEADY_CONSOLIDATION'
  | 'CAUTION_AND_REMEDY';

export interface DomainWindowEvaluation {
  domainCode: string;
  domainTitle: string;
  primaryHouses: number[];
  natalPromiseScore: number;
  dashaActivationScore: number;
  transitConfluenceScore: number;
  overallConfluenceScore: number;
  windowClassification: WindowClassification;
  doubleTransitTriggered: boolean;
  supportingFactors: string[];
  challengingFactors: string[];
}

export interface TemporalForecastWindow {
  windowIndex: number;
  windowLabel: string;
  startUtc: string;
  endUtc: string;
  midpointUtc: string;
  mahadashaLord: GrahaName;
  antardashaLord: GrahaName;
  pratyantardashaLord: GrahaName;
  jupiterTransitSign: ZodiacSign;
  saturnTransitSign: ZodiacSign;
  sadeSatiPhase: string;
  doubleTransitHouses: number[];
  overallWindowScore: number;
  dominantDomain: string;
  domainEvaluations: DomainWindowEvaluation[];
}

export interface DomainTimelineSummary {
  domainCode: string;
  domainTitle: string;
  primaryHouses: number[];
  karakaPlanets: GrahaName[];
  natalPromiseScore: number;
  averageConfluenceScore: number;
  peakScore: number;
  peakWindowLabel: string;
  peakWindowStartUtc: string;
  peakWindowEndUtc: string;
  currentClassification: WindowClassification;
  executiveSummary: string;
}

export interface TemporalAnalysisResponse {
  birthProfileId: string;
  natalUtcDatetimeIso: string;
  anchorUtcDatetimeIso: string;
  natalAscendantSign: ZodiacSign;
  natalMoonSign: ZodiacSign;
  ayanamshaType: string;
  windowCount: number;
  bestOverallWindowLabel: string;
  strongestDomainCode: string;
  domainSummaries: DomainTimelineSummary[];
  timelineWindows: TemporalForecastWindow[];
}

export interface FrameworkChecklistRule {
  ruleCode: string;
  factorCategory:
    | 'HOUSE_AND_LORD'
    | 'KARAKA_STRENGTH'
    | 'DIVISIONAL_VARGA'
    | 'YOGA_OR_DOSHA'
    | 'DASHA_AND_GOCHAR';
  description: string;
  classicalReference: string;
  weight: number;
}

export interface AnalysisFrameworkDefinition {
  categoryCode: string;
  title: string;
  sanskritTitle: string;
  description: string;
  primaryHouses: number[];
  secondaryHouses: number[];
  requiredVargas: string[];
  naisargikaKarakas: GrahaName[];
  specialLagnas: string[];
  keyYogasToCheck: string[];
  checklistRules: FrameworkChecklistRule[];
}

export interface QuestionClassificationResponse {
  frameworkVersion: string;
  questionText: string;
  primaryCategory: string;
  secondaryCategory: string | null;
  confidenceScore: number;
  matchedKeywords: string[];
  activeFramework: AnalysisFrameworkDefinition;
  allFrameworks: AnalysisFrameworkDefinition[];
}

export interface EvidenceGenerationResponse {
  analysisSessionId: string;
  birthProfileId: string;
  frameworkVersion: string;
  questionText: string;
  questionCategory: string;
  primaryHouses: number[];
  requiredVargas: string[];
  totalEvidenceCount: number;
  favorableCount: number;
  challengingCount: number;
  neutralCount: number;
  evidenceItems: EvidenceItem[];
  factorsConsidered: string[];
  timeWindowsSummary: string[];
}

export interface ReasoningStep {
  stepOrder: number;
  stepType:
    | 'NATAL_PROMISE'
    | 'DIVISIONAL_VALIDATION'
    | 'YOGA_CATALYSTS'
    | 'TEMPORAL_TRIGGER'
    | 'SYNTHESIS_AND_CONCLUSION';
  title: string;
  verdict: 'FAVORABLE' | 'MODERATE' | 'CHALLENGING';
  confidenceScore: number;
  narrative: string;
  linkedFactors: string[];
  shastraCitations: string[];
}

export interface ReasoningSynthesisResponse {
  analysisSessionId: string;
  birthProfileId: string;
  frameworkVersion: string;
  questionText: string;
  questionCategory: string;
  primaryHouses: number[];
  overallVerdict: 'FAVORABLE' | 'MODERATE_PROGRESS' | 'CHALLENGING';
  compositeScore: number;
  reasoningSteps: ReasoningStep[];
  classicalRemedies: string[];
}

export interface ToolDefinition {
  name: string;
  description: string;
  parameter_schema: Record<string, unknown>;
}

export interface ToolExecutionRequest {
  tool_name: string;
  arguments: Record<string, unknown>;
}

export interface ToolExecutionResponse {
  tool_name: string;
  success: boolean;
  result?: Record<string, unknown>;
  error?: string;
}

export interface GuardrailResult {
  is_valid: boolean;
  confidence_score: number;
  verified_assertions: string[];
  flagged_discrepancies: string[];
  audited_content: string;
}

export interface LlmToolCall {
  tool_name: string;
  arguments: Record<string, unknown>;
  result?: Record<string, unknown>;
}

export interface AiChatRequest {
  birth_profile_id?: string;
  user_message: string;
  domain_category?: string;
  provider?: string;
  model?: string;
  include_reasoning?: boolean;
  parameters?: Record<string, unknown>;
}

export interface AiChatResponse {
  response: string;
  provider: string;
  model: string;
  guardrail_result: GuardrailResult;
  tools_invoked: LlmToolCall[];
  ground_truth_context: Record<string, unknown>;
  prompt_tokens: number;
  completion_tokens: number;
}

export interface ExplainabilityTrace {
  message_id: string;
  chat_session_id: string;
  birth_profile_id: string;
  ground_truth_context: Record<string, unknown>;
  divisional_charts_consulted: string[];
  active_dasha_period: string;
  reasoning_verdict: string;
  composite_score: number;
  shastric_citations: string[];
  classical_remedies: string[];
  guardrail_result: GuardrailResult;
  provider: string;
  model: string;
}

export interface ChatMessage {
  id: string;
  chat_session_id: string;
  sender_role: 'USER' | 'ASSISTANT' | 'SYSTEM';
  message_content: string;
  explainability_trace?: ExplainabilityTrace;
  prompt_tokens: number;
  completion_tokens: number;
  created_at: string;
}

export interface ChatSession {
  id: string;
  user_id: string;
  birth_profile_id: string;
  title: string;
  rolling_summary?: string;
  created_at: string;
  updated_at: string;
}

export interface CreateChatSessionPayload {
  birth_profile_id: string;
  user_id?: string;
  title?: string;
}

export interface SendChatMessagePayload {
  message: string;
  domain_category?: string;
  include_reasoning?: boolean;
}

export interface AuthUser {
  id: string;
  email: string;
  full_name: string;
  role: string;
  created_at?: string;
}

export interface AuthResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in_ms: number;
  user: AuthUser;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  full_name: string;
  role?: string;
}






