import type { Round } from '../../api/types';
import { BottomBar } from '../../components/BottomBar';
import { Button } from '../../components/Button';
import { Poster } from '../../components/Poster';
import { ClueList } from './ClueList';

type Props = {
  round: Round;
  isLast: boolean;
  onContinue: () => void;
};

/** Shown when a round has ended: the answer, and all of its clues. */
export function RoundResult({ round, isLast, onContinue }: Props) {
  const solved = round.status === 'SOLVED';
  const answer = round.answer;
  if (!answer) return null;

  return (
    <section aria-label="Round result" className="space-y-5 pb-28 sm:pb-0">
      <div className={`rounded-xl p-5 text-white ${solved ? 'bg-emerald-600' : 'bg-rose-600'}`}>
        <p className="text-sm font-extrabold tracking-wide uppercase">
          {solved
            ? `Correct! ${round.attemptsUsed === 1 ? 'First try' : `Attempt ${round.attemptsUsed}`}`
            : 'Out of attempts'}
        </p>
        <div className="mt-4 flex gap-4">
          <Poster path={answer.posterPath} title={answer.title} />
          <div className="min-w-0">
            <p className="text-sm opacity-90">The movie was</p>
            <p className="text-2xl leading-tight font-extrabold break-words">{answer.title}</p>
            <p className="opacity-90">{answer.year}</p>
          </div>
        </div>
      </div>
      <ClueList clues={round.clues} />
      <BottomBar>
        <Button onClick={onContinue} className="w-full">
          {isLast ? 'See results' : 'Next movie'}
        </Button>
      </BottomBar>
    </section>
  );
}
