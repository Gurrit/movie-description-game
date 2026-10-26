import { useStartGame } from '../../api/queries';
import { Button } from '../../components/Button';
import { ClapperIcon } from '../../components/ClapperIcon';
import { ErrorMessage } from '../../components/ErrorMessage';

export function StartScreen() {
  const start = useStartGame();
  return (
    <section aria-label="Start" className="space-y-6 pt-10 text-center">
      <ClapperIcon className="mx-auto size-16" />
      <h2 className="text-3xl font-extrabold tracking-tight">Name that movie</h2>
      <p className="text-slate-600 dark:text-slate-300">
        A new set of movies every day. You start with one clue, and every wrong guess unlocks the next.
      </p>
      <Button onClick={() => start.mutate()} disabled={start.isPending} className="w-full text-lg">
        {start.isPending ? 'Preparing today’s game…' : 'Play today’s game'}
      </Button>
      {start.isPending && (
        <p className="text-sm text-slate-500">
          The first game of the day can take a few seconds to prepare.
        </p>
      )}
      {start.isError && <ErrorMessage error={start.error} onRetry={() => start.mutate()} />}
    </section>
  );
}
