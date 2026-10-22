import { http, HttpResponse } from 'msw';
import { describe, expect, it } from 'vitest';
import { setToken } from '../features/player/token';
import { createFakeBackend } from '../test/fakeBackend';
import { server } from '../test/server';
import { api } from './client';

describe('api client', () => {
  it('creates a player on first use and sends the token', async () => {
    const backend = createFakeBackend();
    server.use(...backend.handlers);

    const result = await api.GET('/game/today');

    expect(backend.calls.createPlayer).toBe(1);
    expect(localStorage.getItem('mdg.token')).toBe('token-1');
    expect(result.response.status).toBe(404); // authenticated, game just not started
  });

  it('creates only one player for concurrent first calls', async () => {
    const backend = createFakeBackend();
    server.use(...backend.handlers);

    await Promise.all([
      api.GET('/game/today'),
      api.GET('/players/me/stats'),
      api.GET('/game/today'),
    ]);

    expect(backend.calls.createPlayer).toBe(1);
  });

  it('reuses a stored token', async () => {
    const backend = createFakeBackend();
    server.use(...backend.handlers);
    await api.GET('/game/today'); // creates token-1

    await api.GET('/players/me/stats');

    expect(backend.calls.createPlayer).toBe(1);
  });

  it('recovers from a token the server no longer knows by creating a player and retrying once', async () => {
    const backend = createFakeBackend();
    server.use(...backend.handlers);
    setToken('stale-token');

    const result = await api.GET('/players/me/stats');

    expect(result.response.status).toBe(200);
    expect(result.data?.gamesPlayed).toBe(3);
    expect(backend.calls.createPlayer).toBe(1);
    expect(localStorage.getItem('mdg.token')).toBe('token-1');
  });

  it('retries a POST with its body after recovering', async () => {
    const backend = createFakeBackend();
    server.use(...backend.handlers);
    await api.POST('/game/today'); // token-1 with a started game
    backend.forgetTokens();

    const result = await api.POST('/game/today/rounds/current/guesses', { body: { tmdbId: 102 } });

    // The new player has no game (404), but the retried request was authorised and still carried its body
    expect(result.response.status).toBe(404);
    expect(backend.calls.guess).toEqual([102]);
  });

  it('does not loop when the server keeps answering 401', async () => {
    let calls = 0;
    server.use(
      http.post('http://localhost:3000/api/players', () =>
        HttpResponse.json({ playerId: 'p', token: 't' }, { status: 201 }),
      ),
      http.get('http://localhost:3000/api/players/me/stats', () => {
        calls++;
        return HttpResponse.json(
          { title: 'no', status: 401, code: 'INVALID_TOKEN' },
          { status: 401 },
        );
      }),
    );

    const result = await api.GET('/players/me/stats');

    expect(result.response.status).toBe(401);
    expect(calls).toBe(2); // the original call and exactly one retry
  });
});
