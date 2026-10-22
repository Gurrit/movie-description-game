import { http, HttpResponse } from 'msw';
import type { Clue, Game, GuessResult, MovieSuggestion, Round, Stats } from '../api/types';

const API = 'http://localhost:3000/api';

export type FakeMovie = { id: number; title: string; year: number };

export const MOVIES: FakeMovie[] = [
  { id: 101, title: 'Alien', year: 1979 },
  { id: 102, title: 'Aliens', year: 1986 },
  { id: 103, title: 'Heat', year: 1995 },
  { id: 104, title: 'Casablanca', year: 1942 },
];

const CLUES = (movie: FakeMovie): Clue[] => [
  { id: 'plot', type: 'PLOT', order: 1, revealed: false, content: { text: `Plot of ${movie.id}` } },
  { id: 'tagline', type: 'TAGLINE', order: 2, revealed: false, content: { text: 'A tagline' } },
  {
    id: 'metadata',
    type: 'METADATA',
    order: 3,
    revealed: false,
    content: { genres: ['Drama'], runtimeMinutes: 117, releaseYear: movie.year },
  },
  {
    id: 'people',
    type: 'PEOPLE',
    order: 4,
    revealed: false,
    content: {
      people: [
        { role: 'DIRECTOR', name: 'Dora Director' },
        { role: 'CAST', name: 'Ann Actor', character: 'Hero' },
      ],
    },
  },
];

type Playthrough = {
  guesses: GuessResult[][];
  revealed: Set<string>[];
  statuses: Round['status'][];
};

/**
 * A small in-memory implementation of the API contract for UI tests. The rounds are [answers] in order;
 * [attempts] wrong guesses fail a round. Tokens are issued by POST /players; unknown tokens get 401.
 */
export function createFakeBackend({ answers = [MOVIES[0]!, MOVIES[2]!], attempts = 2 } = {}) {
  const tokens = new Set<string>();
  const games = new Map<string, Playthrough>();
  const calls = { createPlayer: 0, search: [] as string[], guess: [] as number[] };
  let nextToken = 1;

  const problem = (status: number, code: string, title: string) =>
    HttpResponse.json(
      { title, status, code },
      { status, headers: { 'Content-Type': 'application/problem+json' } },
    );

  const authorized = (request: Request) => {
    const token = request.headers.get('Authorization')?.replace('Bearer ', '') ?? '';
    return tokens.has(token) ? token : null;
  };

  const view = (token: string): Game => {
    const pt = games.get(token)!;
    const current = pt.statuses.findIndex((s) => s === 'ACTIVE');
    const rounds: Round[] = answers.map((movie, i) => {
      const status = pt.statuses[i]!;
      const ended = status === 'SOLVED' || status === 'FAILED';
      return {
        index: i,
        status,
        attemptsUsed: pt.guesses[i]!.length,
        guesses: status === 'PENDING' ? [] : pt.guesses[i]!,
        clues:
          status === 'PENDING'
            ? []
            : CLUES(movie).map((c) => {
                const revealed = pt.revealed[i]!.has(c.id);
                return { ...c, revealed, content: revealed || ended ? c.content : undefined };
              }),
        answer: ended
          ? { tmdbId: movie.id, title: movie.title, year: movie.year, posterPath: '/p.jpg' }
          : undefined,
      };
    });
    return {
      id: 'playthrough-1',
      date: '2026-10-22',
      status: current === -1 ? 'COMPLETE' : 'IN_PROGRESS',
      roundsPerGame: answers.length,
      attemptsPerRound: attempts,
      currentRound: current === -1 ? undefined : current,
      rounds,
    };
  };

  const handlers = [
    http.post(`${API}/players`, () => {
      calls.createPlayer++;
      const token = `token-${nextToken++}`;
      tokens.add(token);
      return HttpResponse.json({ playerId: `player-${token}`, token }, { status: 201 });
    }),
    http.get(`${API}/game/today`, ({ request }) => {
      const token = authorized(request);
      if (!token) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      return games.has(token)
        ? HttpResponse.json(view(token))
        : problem(404, 'GAME_NOT_STARTED', 'Game not started');
    }),
    http.post(`${API}/game/today`, ({ request }) => {
      const token = authorized(request);
      if (!token) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      const created = !games.has(token);
      if (created) {
        games.set(token, {
          guesses: answers.map(() => []),
          revealed: answers.map(() => new Set()),
          statuses: answers.map((_, i) => (i === 0 ? 'ACTIVE' : 'PENDING')),
        });
      }
      return HttpResponse.json(view(token), { status: created ? 201 : 200 });
    }),
    http.post(`${API}/game/today/rounds/current/clues/:clueId/reveal`, ({ request, params }) => {
      const token = authorized(request);
      if (!token) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      const pt = games.get(token);
      if (!pt) return problem(404, 'GAME_NOT_STARTED', 'Game not started');
      pt.revealed[pt.statuses.indexOf('ACTIVE')]!.add(String(params.clueId));
      return HttpResponse.json(view(token));
    }),
    http.post(`${API}/game/today/rounds/current/guesses`, async ({ request }) => {
      const token = authorized(request);
      if (!token) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      const { tmdbId } = (await request.json()) as { tmdbId: number };
      calls.guess.push(tmdbId);
      const pt = games.get(token);
      if (!pt) return problem(404, 'GAME_NOT_STARTED', 'Game not started');
      const round = pt.statuses.indexOf('ACTIVE');
      if (round === -1) return problem(409, 'GAME_COMPLETE', 'Game is complete');
      if (pt.guesses[round]!.some((g) => g.tmdbId === tmdbId))
        return problem(409, 'ALREADY_GUESSED', 'Already guessed');
      const movie = MOVIES.find((m) => m.id === tmdbId)!;
      const correct = tmdbId === answers[round]!.id;
      pt.guesses[round]!.push({ tmdbId, title: movie.title, year: movie.year, correct });
      const ended = correct || pt.guesses[round]!.length >= attempts;
      if (ended) {
        pt.statuses[round] = correct ? 'SOLVED' : 'FAILED';
        if (round + 1 < answers.length) pt.statuses[round + 1] = 'ACTIVE';
      }
      return HttpResponse.json(view(token));
    }),
    http.get(`${API}/movies/search`, ({ request }) => {
      if (!authorized(request)) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      const q = new URL(request.url).searchParams.get('q') ?? '';
      calls.search.push(q);
      const found: MovieSuggestion[] = MOVIES.filter((m) =>
        m.title.toLowerCase().includes(q.toLowerCase()),
      ).map((m) => ({ tmdbId: m.id, title: m.title, year: m.year }));
      return HttpResponse.json(found);
    }),
    http.get(`${API}/players/me/stats`, ({ request }) => {
      if (!authorized(request)) return problem(401, 'INVALID_TOKEN', 'Missing or invalid token');
      const stats: Stats = { gamesPlayed: 3, gamesFullySolved: 1, moviesSolved: 15 };
      return HttpResponse.json(stats);
    }),
  ];

  return {
    handlers,
    calls,
    /** Makes the server forget every token, as if its database was reset. */
    forgetTokens: () => tokens.clear(),
    knows: (token: string) => tokens.has(token),
  };
}
