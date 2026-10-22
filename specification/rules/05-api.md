# API (summary)

Contract-first: the authoritative definition lives in `../../backend/api/openapi.yaml`. This document states required behavior; update the OpenAPI file to match.

All endpoints except `POST /players` require `Authorization: Bearer <token>`. Errors use a common `ErrorResponse`.

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
