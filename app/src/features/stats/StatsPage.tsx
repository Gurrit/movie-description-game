import { useStats } from '../../api/queries';
import { ErrorMessage } from '../../components/ErrorMessage';
import { Spinner } from '../../components/Spinner';

function Figure({ value, label }: { value: number; label: string }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900">
      <dd className="text-4xl font-bold">{value}</dd>
      <dt className="mt-1 text-sm text-slate-500">{label}</dt>
    </div>
  );
}

export function StatsPage() {
  const { data, isPending, isError, error, refetch } = useStats();
  if (isPending) return <Spinner label="Loading your stats…" />;
  if (isError) return <ErrorMessage error={error} onRetry={() => void refetch()} />;
  return (
    <section aria-label="Your stats" className="space-y-4">
      <h2 className="text-lg font-semibold">Your stats</h2>
      <dl className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Figure value={data.gamesPlayed} label="Games played" />
        <Figure value={data.gamesFullySolved} label="Fully solved" />
        <Figure value={data.moviesSolved} label="Movies solved" />
      </dl>
    </section>
  );
}
