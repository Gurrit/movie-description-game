import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ApiError } from './ApiError';
import { api } from './client';
import type { Game, MovieSuggestion, Problem, Stats } from './types';

type Result<T> = { data?: T; error?: unknown; response: Response };

function unwrap<T>(result: Result<T>): T {
  if (result.data !== undefined) return result.data;
  throw ApiError.fromProblem(result.response.status, result.error as Partial<Problem> | undefined);
}

export const gameKey = ['game', 'today'] as const;

/** Today's game of the player, or null if they have not started it. The server is the single source of truth. */
export function useTodaysGame() {
  return useQuery({
    queryKey: gameKey,
    queryFn: async (): Promise<Game | null> => {
      const result = await api.GET('/game/today');
      if (
        result.response.status === 404 &&
        (result.error as Problem | undefined)?.code === 'GAME_NOT_STARTED'
      ) {
        return null;
      }
      return unwrap(result);
    },
  });
}

/** Every game mutation returns the updated game, which replaces the cached one. */
function useGameMutation<V>(call: (variables: V) => Promise<Result<Game>>) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: async (variables: V) => unwrap(await call(variables)),
    onSuccess: (game) => client.setQueryData(gameKey, game),
    onError: (error) => {
      // The game moved on elsewhere (another tab); show the real state
      if (error instanceof ApiError && error.status === 409)
        void client.invalidateQueries({ queryKey: gameKey });
    },
  });
}

export function useStartGame() {
  return useGameMutation<void>(() => api.POST('/game/today'));
}

export function useSubmitGuess() {
  return useGameMutation<number>((tmdbId) =>
    api.POST('/game/today/rounds/current/guesses', { body: { tmdbId } }),
  );
}

/** Gives up an attempt without a guess; the next clue unlocks, as after a wrong guess. */
export function useSkipClue() {
  return useGameMutation<void>(() => api.POST('/game/today/rounds/current/skip'));
}

/** A random movie to try as a guess, other than the [excluded] ones (already guessed in the round). */
export function useRandomMovie() {
  return useMutation({
    mutationFn: async (excluded: ReadonlySet<number>): Promise<MovieSuggestion> => {
      let movie = unwrap(await api.GET('/movies/random'));
      // Pool pages are large, so a repeat is rare; a few retries are plenty
      for (let tries = 0; excluded.has(movie.tmdbId) && tries < 5; tries++) {
        movie = unwrap(await api.GET('/movies/random'));
      }
      return movie;
    },
  });
}

export const MIN_SEARCH_LENGTH = 2;

export function useMovieSearch(query: string) {
  const q = query.trim();
  return useQuery({
    queryKey: ['movies', 'search', q.toLowerCase()],
    queryFn: async (): Promise<MovieSuggestion[]> =>
      unwrap(await api.GET('/movies/search', { params: { query: { q } } })),
    enabled: q.length >= MIN_SEARCH_LENGTH,
    staleTime: 10 * 60 * 1000,
    placeholderData: keepPreviousData,
  });
}

export function useStats() {
  return useQuery({
    queryKey: ['stats'],
    queryFn: async (): Promise<Stats> => unwrap(await api.GET('/players/me/stats')),
  });
}
