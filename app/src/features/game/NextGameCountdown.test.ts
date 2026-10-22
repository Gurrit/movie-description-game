import { describe, expect, it } from 'vitest';
import { formatCountdown, msUntilNextGame } from './NextGameCountdown';

describe('next game countdown', () => {
  it('counts down to the next UTC midnight', () => {
    expect(msUntilNextGame(new Date('2026-10-22T23:00:00Z'))).toBe(3600_000);
    expect(msUntilNextGame(new Date('2026-12-31T12:00:00Z'))).toBe(12 * 3600_000);
  });

  it('formats as HH:MM:SS', () => {
    expect(formatCountdown(3661_000)).toBe('01:01:01');
    expect(formatCountdown(-5)).toBe('00:00:00');
  });
});
