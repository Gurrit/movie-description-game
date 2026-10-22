import { useTodaysGame } from '../../api/queries';
import { ErrorMessage } from '../../components/ErrorMessage';
import { Spinner } from '../../components/Spinner';
import { ActiveGame } from './ActiveGame';
import { StartScreen } from './StartScreen';

export function GamePage() {
  const { data: game, isPending, isError, error, refetch } = useTodaysGame();

  if (isPending) return <Spinner label="Loading your game…" />;
  if (isError) return <ErrorMessage error={error} onRetry={() => void refetch()} />;
  if (game === null) return <StartScreen />;
  return <ActiveGame key={game.id} game={game} />;
}
