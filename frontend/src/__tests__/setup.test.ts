import { describe, it, expect } from 'vitest';
import type { EvidenceItem } from '../types/astrology';

describe('Phase 1 Frontend Type & Test Harness Verification', () => {
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
});
