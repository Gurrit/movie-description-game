import type { Clue } from '../../api/types';
import { ClueContentView, clueLabel } from './ClueContentView';

type Props = {
  clues: Clue[];
  /** Id of the clue being revealed right now, if any. */
  revealing?: string | null;
  onReveal?: (clueId: string) => void;
};

const CARD = 'rounded-lg border-2 p-4';

export function ClueList({ clues, revealing = null, onReveal }: Props) {
  return (
    <ul className="space-y-3">
      {clues.map((clue) => (
        <li key={clue.id}>
          {clue.content ? (
            <div
              className={`${CARD} border-slate-300 bg-slate-50 motion-safe:animate-reveal dark:border-slate-700 dark:bg-slate-900`}
            >
              <h3 className="mb-1 text-xs font-bold tracking-wide text-slate-500 uppercase">
                {clueLabel(clue)}
              </h3>
              <ClueContentView clue={clue} />
            </div>
          ) : (
            <button
              type="button"
              aria-label={`Reveal ${clueLabel(clue).toLowerCase()}`}
              disabled={!onReveal || revealing !== null}
              onClick={() => onReveal?.(clue.id)}
              className={`${CARD} flex min-h-16 w-full items-center justify-between border-dashed border-slate-400 text-left transition hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sky-500 disabled:opacity-60 dark:border-slate-600 dark:hover:bg-slate-900`}
            >
              <span className="text-xs font-bold tracking-wide text-slate-500 uppercase">
                {clueLabel(clue)}
              </span>
              <span className="text-sm font-bold" aria-hidden>
                {revealing === clue.id ? 'Revealing…' : 'Tap to reveal'}
              </span>
            </button>
          )}
        </li>
      ))}
    </ul>
  );
}
