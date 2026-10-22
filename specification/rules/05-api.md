# API (summary)

Contract-first: the authoritative definition is `api/openapi.yaml` (internal operational endpoint: `api/internal.yaml`, documentation only). This document gives the intent; if they disagree, the OpenAPI file wins.

**Resolved:** every mutating game call (`start`, `reveal`, `guess`) returns the full updated `Game`. A guess that ends a round moves the game on automatically; the ended round carries its `answer`, and the client shows the result until the player continues (client-only UI state). There is no `next` endpoint, and no separate `/config`: `roundsPerGame` and `attemptsPerRound` are part of `Game`.

All endpoints except `POST /players` require `Authorization: Bearer <token>`. Errors use RFC 9457 `application/problem+json`.

| Method & path | Purpose |
|---|---|
| `POST /players` | Create a player. Returns `{playerId, token}`. |
| `GET /game/today` | Today's game for the player: returns the existing game, or `404` if not started. Includes the date and the config values the UI needs (`roundsPerGame`, `attemptsPerRound`). |
| `POST /game/today` | Start today's game (idempotent: returns the existing one). Triggers lazy generation of the set if needed. |
| `POST /game/today/rounds/current/clues/{clueId}/reveal` | Reveal a clue in the active round. Idempotent. Returns the clue content. |
| `POST /game/today/rounds/current/guesses` | Body `{tmdbId}`. Records an attempt. Returns `{correct, attemptsUsed, attemptsRemaining, roundStatus, answer?}`; `answer` (title, year, posterPath) only when the round has ended. 409 if the round is over or the movie was already guessed this round. |
| `POST /game/today/rounds/current/next` *(or implicit)* | Advance to the next round after a round ended. Alternative: the guess response already moves the game on, and the client shows the answer from the response. Pick one when writing the OpenAPI. |
| `GET /movies/search?q=` | Autocomplete, proxying TMDB (see [03](03-challenge-generation.md#autocomplete)). |
| `GET /players/me/stats` | Personal stats ([04](04-players-and-stats.md#stats-scoreboard)). |
| `GET /config` *(optional)* | Public game parameters, if not embedded in the game resource. |

## Response shape of a game (client view)
```
Game {
  date, status, currentRound,
  rounds: [ {
    index, status,
    attemptsUsed, attemptsAllowed,
    guesses: [ {tmdbId, title, year, correct} ],     // only for rounds already played/active
    clues: [ {id, type, order, revealed, content?} ], // content only when revealed
    answer?: {tmdbId, title, year, posterPath}        // only when the round ended
  } ]                                                  // future rounds: index + status PENDING only
}
```

## Non-functional
- No endpoint returns the answer or unrevealed clue content of a round that has not ended.
- Rate limit `/movies/search` and `/players` per IP.
- Concurrency: guesses are applied atomically (e.g. conditional update on attempt count) so double-submits don't burn two attempts.
