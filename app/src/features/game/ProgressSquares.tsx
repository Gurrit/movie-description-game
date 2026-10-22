import type { Round } from '../../api/types';

const STYLE: Record<Round['status'], string> = {
  PENDING: 'bg-slate-200 text-slate-500 dark:bg-slate-800 dark:text-slate-400',
  ACTIVE: 'bg-amber-400 text-slate-900',
  SOLVED: 'bg-emerald-600 text-white',
  FAILED: 'bg-rose-600 text-white',
};

const LABEL: Record<Round['status'], string> = {
  PENDING: 'not played yet',
  ACTIVE: 'in progress',
  SOLVED: 'solved',
  FAILED: 'failed',
};

/** One numbered square per movie, coloured by how that round went. */
export function ProgressSquares({ rounds }: { rounds: Round[] }) {
  return (
    <ol aria-label="Progress" className="flex gap-1.5">
      {rounds.map((round) => (
        <li
          key={round.index}
          title={`Movie ${round.index + 1}: ${LABEL[round.status]}`}
          className={`flex size-8 items-center justify-center rounded text-sm font-bold ${STYLE[round.status]}`}
        >
          <span aria-hidden>{round.index + 1}</span>
          <span className="sr-only">
            Movie {round.index + 1}: {LABEL[round.status]}
          </span>
        </li>
      ))}
    </ol>
  );
}
