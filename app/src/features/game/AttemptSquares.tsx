/** The attempts of a round as squares: used ones filled red, remaining ones outlined. */
export function AttemptSquares({ allowed, used }: { allowed: number; used: number }) {
  const remaining = allowed - used;
  return (
    <div className="flex items-center gap-3">
      <div aria-hidden className="flex gap-1.5">
        {Array.from({ length: allowed }, (_, i) => (
          <span
            key={i}
            className={`size-5 rounded-sm border-2 ${
              i < used
                ? 'border-rose-600 bg-rose-600'
                : 'border-slate-400 bg-transparent dark:border-slate-500'
            }`}
          />
        ))}
      </div>
      <p aria-live="polite" className="text-sm font-medium text-slate-600 dark:text-slate-300">
        {remaining === 1 ? '1 attempt left' : `${remaining} attempts left`}
      </p>
    </div>
  );
}
