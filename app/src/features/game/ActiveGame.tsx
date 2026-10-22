import { useState } from 'react';
import { isEnded, type Game } from '../../api/types';
import { GameSummary } from './GameSummary';
import { RoundResult } from './RoundResult';
import { RoundView } from './RoundView';

const endedCount = (game: Game) => game.rounds.filter(isEnded).length;

/**
 * A started game. The server decides everything; the only state kept here is how many ended rounds the
 * player has already looked at, so the result of a round that just ended is shown before moving on.
 * Mounted once the game is loaded, so rounds that ended before a page load are not shown again.
 */
export function ActiveGame({ game }: { game: Game }) {
  const [seen, setSeen] = useState(() => endedCount(game));

  const resultRound = game.rounds[seen];
  if (resultRound && isEnded(resultRound)) {
    return (
      <RoundResult
        round={resultRound}
        isLast={seen + 1 >= game.roundsPerGame}
        onContinue={() => setSeen(seen + 1)}
      />
    );
  }

  if (game.status === 'COMPLETE') return <GameSummary game={game} />;

  const current = game.rounds[game.currentRound ?? 0];
  return current ? <RoundView game={game} round={current} /> : null;
}
