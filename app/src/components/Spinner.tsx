export function Spinner({ label }: { label: string }) {
  return (
    <div role="status" className="flex items-center justify-center gap-3 py-12 text-slate-500">
      <span
        aria-hidden
        className="size-5 animate-spin rounded-full border-2 border-slate-300 border-t-slate-700 dark:border-slate-700 dark:border-t-slate-300"
      />
      <span>{label}</span>
    </div>
  );
}
