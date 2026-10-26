import type { Clue } from '../../api/types';
import { ClueContentView, clueLabel } from './ClueContentView';

const CARD = 'rounded-lg border-2 p-4';

/** The clues of a round in unlock order: the ones earned so far with their content, the rest as locked cards. */
export function ClueList({ clues }: { clues: Clue[] }) {
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
            <div
              className={`${CARD} flex min-h-16 items-center justify-between border-dashed border-slate-300 text-slate-400 dark:border-slate-700 dark:text-slate-500`}
            >
              <span className="text-xs font-bold tracking-wide uppercase">{clueLabel(clue)}</span>
              <span className="text-sm font-bold">Locked</span>
            </div>
          )}
        </li>
      ))}
    </ul>
  );
}
