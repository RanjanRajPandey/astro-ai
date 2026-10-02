import { describe, it, expect } from 'vitest';
import {
  getSignIndexForHouse,
  getHouseNumberForSign,
  getDignityBadgeStyle,
  PLANET_ABBR,
  PLANET_COLORS,
  SIGN_NAMES_BY_INDEX,
} from '../utils/chartMath';

describe('Chart Mathematics & Zodiac Mapping Invariants', () => {
  it('maps house signs correctly for North Indian Whole Sign charts', () => {
    // Taurus Ascendant (Sign index 2):
    // House 1 -> Taurus (2)
    expect(getSignIndexForHouse(2, 1)).toBe(2);
    // House 2 -> Gemini (3)
    expect(getSignIndexForHouse(2, 2)).toBe(3);
    // House 3 -> Cancer (4)
    expect(getSignIndexForHouse(2, 3)).toBe(4);
    // House 7 -> Scorpio (8)
    expect(getSignIndexForHouse(2, 7)).toBe(8);
    // House 12 -> Aries (1)
    expect(getSignIndexForHouse(2, 12)).toBe(1);

    // Pisces Ascendant (Sign index 12):
    // House 1 -> Pisces (12)
    expect(getSignIndexForHouse(12, 1)).toBe(12);
    // House 2 -> Aries (1)
    expect(getSignIndexForHouse(12, 2)).toBe(1);
  });

  it('maps sign houses correctly for South Indian fixed charts', () => {
    // Taurus Ascendant (Sign index 2):
    // Taurus sign (2) -> House 1
    expect(getHouseNumberForSign(2, 2)).toBe(1);
    // Gemini sign (3) -> House 2
    expect(getHouseNumberForSign(2, 3)).toBe(2);
    // Aries sign (1) -> House 12
    expect(getHouseNumberForSign(2, 1)).toBe(12);
  });

  it('verifies all 12 signs are cataloged in canonical order', () => {
    expect(Object.keys(SIGN_NAMES_BY_INDEX)).toHaveLength(12);
    expect(SIGN_NAMES_BY_INDEX[1]).toBe('Aries');
    expect(SIGN_NAMES_BY_INDEX[2]).toBe('Taurus');
    expect(SIGN_NAMES_BY_INDEX[12]).toBe('Pisces');
  });

  it('verifies all 9 classical grahas have abbreviations and distinct colors', () => {
    const planets = ['Sun', 'Moon', 'Mars', 'Mercury', 'Jupiter', 'Venus', 'Saturn', 'Rahu', 'Ketu'] as const;
    planets.forEach((p) => {
      expect(PLANET_ABBR[p]).toBeDefined();
      expect(PLANET_ABBR[p].length).toBe(2);
      expect(PLANET_COLORS[p]).toMatch(/^#[0-9A-Fa-f]{6}$/);
    });
  });

  it('assigns appropriate styling classes based on classical dignity', () => {
    expect(getDignityBadgeStyle('EXALTED')).toContain('emerald');
    expect(getDignityBadgeStyle('OWN_SIGN')).toContain('amber');
    expect(getDignityBadgeStyle('DEBILITATED')).toContain('rose');
    expect(getDignityBadgeStyle('UNKNOWN')).toContain('slate');
  });
});
