import createClient from 'openapi-fetch';
import { clearToken, getToken, setToken } from '../features/player/token';
import type { paths } from './schema';

// Absolute, so it also works outside a browser page (tests). In production the reverse proxy
// serves the API on the same origin as the app.
const BASE_URL = `${globalThis.location?.origin ?? 'http://localhost'}/api`;

let pending: Promise<string> | null = null;

/** The player's token; creates a new player first if there is none. Concurrent callers share one request. */
export function ensureToken(): Promise<string> {
  const existing = getToken();
  if (existing) return Promise.resolve(existing);
  pending ??= createPlayer().finally(() => {
    pending = null;
  });
  return pending;
}

async function createPlayer(): Promise<string> {
  const response = await fetch(`${BASE_URL}/players`, { method: 'POST' });
  if (!response.ok) throw new Error(`Could not create a player (${response.status})`);
  const { token } = (await response.json()) as { token: string };
  setToken(token);
  return token;
}

function withToken(request: Request, token: string): Request {
  const headers = new Headers(request.headers);
  headers.set('Authorization', `Bearer ${token}`);
  return new Request(request, { headers });
}

/**
 * Adds the bearer token to every call. If the server no longer knows the token (401), the player is
 * recreated and the call is retried once; creating a player itself needs no token.
 */
async function authFetch(input: Request): Promise<Response> {
  if (new URL(input.url).pathname.endsWith('/players') && input.method === 'POST')
    return fetch(input);

  const token = await ensureToken();
  const response = await fetch(withToken(input.clone(), token));
  if (response.status !== 401) return response;

  // Only forget the token that was rejected: a concurrent call may already have replaced it
  if (getToken() === token) clearToken();
  return fetch(withToken(input.clone(), await ensureToken()));
}

export const api = createClient<paths>({ baseUrl: BASE_URL, fetch: authFetch });
