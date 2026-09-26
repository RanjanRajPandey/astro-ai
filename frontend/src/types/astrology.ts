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
