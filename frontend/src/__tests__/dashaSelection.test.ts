import { describe, it, expect } from 'vitest';

describe('Vimshottari Dasha Active State and Profile Prioritization', () => {
  function isNodeActive(node: any): boolean {
    if (!node) return false;
    return Boolean(node.isCurrentlyActive ?? node.is_currently_active);
  }

  function getActiveMahaIndex(mahadashas: any[]): number {
    const idx = mahadashas.findIndex(isNodeActive);
    return idx >= 0 ? idx : 0;
  }

  it('correctly resolves active status with camelCase and snake_case fields', () => {
    expect(isNodeActive({ isCurrentlyActive: true })).toBe(true);
    expect(isNodeActive({ isCurrentlyActive: false })).toBe(false);
    expect(isNodeActive({ is_currently_active: true })).toBe(true);
    expect(isNodeActive({ is_currently_active: false })).toBe(false);
    expect(isNodeActive({})).toBe(false);
    expect(isNodeActive(null)).toBe(false);
  });

  it('selects Saturn as active Mahadasha index when Saturn is active', () => {
    const mahadashas = [
      { planet: 'Jupiter', isCurrentlyActive: false, durationYears: 16 },
      { planet: 'Saturn', isCurrentlyActive: true, durationYears: 19 },
      { planet: 'Mercury', isCurrentlyActive: false, durationYears: 17 },
      { planet: 'Ketu', isCurrentlyActive: false, durationYears: 7 },
      { planet: 'Venus', isCurrentlyActive: false, durationYears: 20 },
      { planet: 'Sun', isCurrentlyActive: false, durationYears: 6 },
      { planet: 'Moon', isCurrentlyActive: false, durationYears: 10 },
      { planet: 'Mars', isCurrentlyActive: false, durationYears: 7 },
      { planet: 'Rahu', isCurrentlyActive: false, durationYears: 18 },
    ];

    const activeIdx = getActiveMahaIndex(mahadashas);
    expect(activeIdx).toBe(1);
    expect(mahadashas[activeIdx].planet).toBe('Saturn');
  });

  it('prioritizes Ranjan Raj Pandey profile when no stored ID exists in localStorage', () => {
    const existing = [
      { id: 'profile-aarav', name: 'Aarav Sharma (Reference Kundli)', dateOfBirth: '1990-05-15' },
      { id: 'profile-ranjan', name: 'Ranjan Raj Pandey', dateOfBirth: '2004-08-22' },
    ];

    const storedId: string | null = null;
    const storedMatch = storedId ? existing.find((p) => p.id === storedId) : null;
    const ranjanMatch = existing.find(
      (p) =>
        p.name.toLowerCase().includes('ranjan') ||
        p.name.toLowerCase().includes('pandey'),
    );
    const targetProfile = storedMatch || ranjanMatch || existing[0];

    expect(targetProfile.id).toBe('profile-ranjan');
    expect(targetProfile.name).toBe('Ranjan Raj Pandey');
  });

  it('respects stored ID from localStorage if already saved', () => {
    const existing = [
      { id: 'profile-aarav', name: 'Aarav Sharma (Reference Kundli)', dateOfBirth: '1990-05-15' },
      { id: 'profile-ranjan', name: 'Ranjan Raj Pandey', dateOfBirth: '2004-08-22' },
    ];

    const storedId = 'profile-ranjan';
    const storedMatch = storedId ? existing.find((p) => p.id === storedId) : null;
    const ranjanMatch = existing.find(
      (p) =>
        p.name.toLowerCase().includes('ranjan') ||
        p.name.toLowerCase().includes('pandey'),
    );
    const targetProfile = storedMatch || ranjanMatch || existing[0];

    expect(targetProfile.id).toBe('profile-ranjan');
  });
});
