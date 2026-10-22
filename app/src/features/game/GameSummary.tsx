import { Link } from 'react-router-dom';
import type { Game } from '../../api/types';
import { NextGameCountdown } from './NextGameCountdown';

export function GameSummary({ game }: { game: Game }) {
  const solved = game.rounds.filter((r) => r.status === 'SOLVED').length;
  return (
    <section aria-label="Game summary" className="space-y-5">
      <div className="rounded-xl bg-slate-900 p-6 text-center text-white dark:bg-white dark:text-slate-900">
        <p className="text-sm font-bold tracking-wide uppercase opacity-70">
          Today's game · {game.date}
        </p>
        <p className="mt-1 text-5xl font-extrabold">
          {solved} / {game.roundsPerGame}
        </p>
        <p className="font-medium opacity-80">movies found</p>
      </div>
      <ol className="space-y-2">
        {game.rounds.map((round) => {
          const ok = round.status === 'SOLVED';
          return (
            <li
              key={round.index}
              className="flex items-center gap-3 rounded-lg border-2 border-slate-200 px-3 py-2.5 dark:border-slate-800"
            >
              <span
                aria-hidden
                className={`flex size-8 shrink-0 items-center justify-center rounded text-sm font-bold text-white ${ok ? 'bg-emerald-600' : 'bg-rose-600'}`}
              >
                {ok ? '✓' : '✗'}
              </span>
              <span className="sr-only">{ok ? 'Solved: ' : 'Failed: '}</span>
              <span className="min-w-0 flex-1 font-semibold">
                {round.answer?.title}
                <span className="font-normal text-slate-500"> ({round.answer?.year})</span>
              </span>
              <span className="shrink-0 text-sm text-slate-500">
                {round.attemptsUsed} {round.attemptsUsed === 1 ? 'attempt' : 'attempts'}
              </span>
            </li>
          );
        })}
      </ol>
      <div className="flex items-center justify-between">
        <NextGameCountdown />
        <Link to="/stats" className="font-bold underline">
          Your stats
        </Link>
      </div>
    </section>
  );
}
