import type { GrahaName, ZodiacSign } from '../types/astrology';

export const PLANET_ABBR: Record<GrahaName, string> = {
  Sun: 'Su',
  Moon: 'Mo',
  Mars: 'Ma',
  Mercury: 'Me',
  Jupiter: 'Ju',
  Venus: 'Ve',
  Saturn: 'Sa',
  Rahu: 'Ra',
  Ketu: 'Ke',
};

export const PLANET_COLORS: Record<GrahaName, string> = {
  Sun: '#F59E0B',
  Moon: '#E2E8F0',
  Mars: '#EF4444',
  Mercury: '#10B981',
  Jupiter: '#FBBF24',
  Venus: '#EC4899',
  Saturn: '#60A5FA',
  Rahu: '#A78BFA',
  Ketu: '#F97316',
};

export const SIGN_NAMES_BY_INDEX: Record<number, ZodiacSign> = {
  1: 'Aries',
  2: 'Taurus',
  3: 'Gemini',
  4: 'Cancer',
  5: 'Leo',
  6: 'Virgo',
  7: 'Libra',
  8: 'Scorpio',
  9: 'Sagittarius',
  10: 'Capricorn',
  11: 'Aquarius',
  12: 'Pisces',
};

/**
 * Given Ascendant sign index (1..12) and House number (1..12),
 * returns the Rashi sign index (1..12) for that house in a North Indian chart.
 */
export function getSignIndexForHouse(ascendantSignIndex: number, houseNumber: number): number {
  return ((ascendantSignIndex - 1 + (houseNumber - 1)) % 12) + 1;
}

/**
 * Given Ascendant sign index (1..12) and a Rashi sign index (1..12),
 * returns the House number (1..12) for that sign in a South Indian chart.
 */
export function getHouseNumberForSign(ascendantSignIndex: number, signIndex: number): number {
  return ((signIndex - ascendantSignIndex + 12) % 12) + 1;
}

export function getDignityBadgeStyle(dignity: string): string {
  switch (dignity) {
    case 'EXALTED':
      return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
    case 'MOOLATRIKONA':
    case 'OWN_SIGN':
      return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
    case 'GREAT_FRIEND':
    case 'FRIEND':
      return 'bg-sky-500/20 text-sky-300 border-sky-500/40';
    case 'DEBILITATED':
      return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
    case 'ENEMY':
    case 'GREAT_ENEMY':
      return 'bg-orange-500/20 text-orange-300 border-orange-500/40';
    default:
      return 'bg-slate-700/50 text-slate-300 border-slate-600';
  }
}
