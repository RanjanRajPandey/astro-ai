import { describe, it, expect } from 'vitest';
import type { EvidenceItem } from '../types/astrology';
import { getSignIndexForHouse, getHouseNumberForSign } from '../utils/chartMath';

describe('Phase 5 Frontend Kundli Chart Math & Type Verification', () => {
  it('validates structured EvidenceItem contract', () => {
    const sampleEvidence: EvidenceItem = {
      factor: '7th Lord Venus in Exaltation',
      category: 'MARRIAGE',
      observation: 'Venus at 18° Pisces in 5th house',
      rule: 'BPHS Ch. 18',
      effect: 'Supports harmonious partnership',
      classification: 'SUPPORTING',
      importance: 'HIGH',
      source: 'NATAL',
    };
    expect(sampleEvidence.classification).toBe('SUPPORTING');
    expect(sampleEvidence.source).toBe('NATAL');
  });

  it('computes North Indian sign-to-house rotation accurately for Virgo (6) Ascendant', () => {
    // Virgo Ascendant (signIndex = 6): House 1 = 6 (Virgo), House 7 = 12 (Pisces), House 8 = 1 (Aries), House 9 = 2 (Taurus)
    expect(getSignIndexForHouse(6, 1)).toBe(6);
    expect(getSignIndexForHouse(6, 7)).toBe(12);
    expect(getSignIndexForHouse(6, 8)).toBe(1);
    expect(getSignIndexForHouse(6, 9)).toBe(2);
  });

  it('computes South Indian house-for-sign mapping accurately for Virgo (6) Ascendant', () => {
    expect(getHouseNumberForSign(6, 6)).toBe(1);
    expect(getHouseNumberForSign(6, 2)).toBe(9);
    expect(getHouseNumberForSign(6, 10)).toBe(5);
  });
});
