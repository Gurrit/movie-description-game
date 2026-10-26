import { useMemo, useState } from 'react';
import { useSkipClue, useSubmitGuess } from '../../api/queries';
import type { Game, Round } from '../../api/types';
import { BottomBar } from '../../components/BottomBar';
import { ErrorMessage } from '../../components/ErrorMessage';
import { AttemptSquares } from './AttemptSquares';
import { ClueList } from './ClueList';
import { GuessInput } from './GuessInput';
import { ProgressSquares } from './ProgressSquares';

/** The active round: the clues unlocked so far, previous wrong guesses, and the guess box with its skip and random buttons. */
export function RoundView({ game, round }: { game: Game; round: Round }) {
  const guess = useSubmitGuess();
  const skip = useSkipClue();
  const [lastGuess, setLastGuess] = useState<string | null>(null);
  const guessedIds = useMemo(() => new Set(round.guesses.map((g) => g.tmdbId)), [round.guesses]);

  return (
    <section
      aria-label={`Movie ${round.index + 1} of ${game.roundsPerGame}`}
      className="space-y-5 pb-28 sm:pb-0"
    >
      <div className="space-y-3">
        <ProgressSquares rounds={game.rounds} />
        <h2 className="text-lg font-extrabold">
          Movie {round.index + 1} of {game.roundsPerGame}
        </h2>
      </div>

      <ClueList clues={round.clues} />

      <div className="space-y-3">
        <AttemptSquares allowed={game.attemptsPerRound} used={round.attemptsUsed} />
        {round.guesses.length > 0 && (
          <ul aria-label="Your wrong guesses" className="flex flex-wrap gap-2">
            {round.guesses.map((g) => (
              <li
                key={g.tmdbId}
                className="rounded-full bg-rose-100 px-3 py-1 text-sm font-medium text-rose-900 dark:bg-rose-950 dark:text-rose-100"
              >
                ✗ {g.title}
                {g.year != null && ` (${g.year})`}
              </li>
            ))}
          </ul>
        )}
        {guess.isPending && <p className="text-sm text-slate-500">Checking {lastGuess}…</p>}
        {guess.isError && <ErrorMessage error={guess.error} />}
        {skip.isError && <ErrorMessage error={skip.error} />}
      </div>

      <BottomBar>
        <GuessInput
          guessedIds={guessedIds}
          disabled={guess.isPending || skip.isPending}
          lastAttempt={round.attemptsUsed + 1 >= game.attemptsPerRound}
          onSkip={() => skip.mutate()}
          onGuess={(movie) => {
            setLastGuess(movie.title);
            guess.mutate(movie.tmdbId);
          }}
        />
      </BottomBar>
    </section>
  );
}
