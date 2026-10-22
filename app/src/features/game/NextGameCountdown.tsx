import { useEffect, useState } from 'react';

/** Milliseconds until the next UTC midnight, when the next game becomes available. */
export function msUntilNextGame(now: Date): number {
  const next = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate() + 1);
  return next - now.getTime();
}

export function formatCountdown(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000));
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${pad(Math.floor(total / 3600))}:${pad(Math.floor((total % 3600) / 60))}:${pad(total % 60)}`;
}

export function NextGameCountdown() {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);
  return (
    <p className="text-sm text-slate-600 dark:text-slate-300">
      Next game in{' '}
      <time className="font-mono font-semibold">{formatCountdown(msUntilNextGame(now))}</time>
    </p>
  );
}
