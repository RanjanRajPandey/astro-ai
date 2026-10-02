import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  getStoredAccessToken,
  setStoredAuth,
  clearStoredAuth,
  getStoredUser,
} from '../services/api';
import type { AuthResponse, AuthUser } from '../types/astrology';

// In Node test environment, polyfill localStorage if not available
const store: Record<string, string> = {};
const mockLocalStorage = {
  getItem: (key: string) => store[key] ?? null,
  setItem: (key: string, value: string) => {
    store[key] = value.toString();
  },
  removeItem: (key: string) => {
    delete store[key];
  },
  clear: () => {
    Object.keys(store).forEach((k) => delete store[k]);
  },
  length: 0,
  key: () => null,
};
Object.defineProperty(globalThis, 'localStorage', {
  value: mockLocalStorage,
  writable: true,
});

describe('Auth & Storage Service', () => {
  beforeEach(() => {
    globalThis.localStorage.clear();
    vi.restoreAllMocks();
  });

  it('manages JWT access tokens and user profile persistence in localStorage', () => {
    expect(getStoredAccessToken()).toBeNull();
    expect(getStoredUser()).toBeNull();

    const mockUser: AuthUser = {
      id: 'user-123',
      email: 'astrologer@astroai.com',
      full_name: 'Pandit Shastri',
      role: 'ROLE_ASTROLOGER',
      created_at: new Date().toISOString(),
    };

    const mockAuthResponse: AuthResponse = {
      access_token: 'test-jwt-token-xyz',
      refresh_token: 'test-refresh-token-abc',
      token_type: 'Bearer',
      expires_in_ms: 86400000,
      user: mockUser,
    };

    setStoredAuth(mockAuthResponse);

    expect(getStoredAccessToken()).toBe('test-jwt-token-xyz');
    expect(localStorage.getItem('astro_refresh_token')).toBe('test-refresh-token-abc');

    const storedUser = getStoredUser();
    expect(storedUser).not.toBeNull();
    expect(storedUser?.email).toBe('astrologer@astroai.com');
    expect(storedUser?.role).toBe('ROLE_ASTROLOGER');

    clearStoredAuth();
    expect(getStoredAccessToken()).toBeNull();
    expect(getStoredUser()).toBeNull();
  });
});
