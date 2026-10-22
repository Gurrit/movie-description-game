import { afterEach, describe, expect, it, vi } from 'vitest';
import { clearToken, getToken, isTokenPersistent, setToken } from './token';

describe('token storage', () => {
  afterEach(() => {
    vi.restoreAllMocks();
    clearToken();
  });

  it('stores and clears the token in localStorage', () => {
    setToken('abc');
    expect(localStorage.getItem('mdg.token')).toBe('abc');
    expect(getToken()).toBe('abc');
    clearToken();
    expect(getToken()).toBeNull();
    expect(localStorage.getItem('mdg.token')).toBeNull();
  });

  it('keeps the token in memory when localStorage throws', () => {
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('blocked');
    });
    setToken('mem');
    expect(getToken()).toBe('mem');
    expect(isTokenPersistent()).toBe(false);
  });
});
