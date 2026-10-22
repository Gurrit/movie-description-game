# Players, Sessions and Stats

## Identity
- No accounts, no login. A **player** is created by the backend on first visit and identified by an **opaque token** (random, ≥128 bits, stored hashed server-side).
- Frontend flow: no token in `localStorage` → `POST /players` → store the token → send it as `Authorization: Bearer <token>` on all calls.
- Reloading the page (or returning another day) with the same browser keeps the player and their history. Clearing storage or switching browser means a new player, and the old history is unreachable. This is accepted; no recovery mechanism is planned.
- An invalid or unknown token yields 401; the frontend then discards it and creates a new player.

## Persistence (PostgreSQL, never purged; table schema in [../tech/03-database.md](../tech/03-database.md))
- `players`: id, token hash, createdAt, lastSeenAt
- Daily sets: one per UTC date, holding 7 ordered movie snapshots (id, title, original title, year, poster path, clues).
- Games: one per player per date, with status, current round, per-round status, ordered guesses and revealed clues (with timestamps). Unique on (player, date).

Since games reference movies by id inside the daily set, past games remain reconstructable.

## Stats ("scoreboard")
Stats are **personal only**; nothing is shared or ranked between players. A minimal stats screen shows:

- Games played
- Games fully solved (all rounds solved)
- Total movies solved (across all games)

Stats are derived from `games` (computed with an aggregation or maintained counters; implementation choice) and are available via `GET /players/me/stats`. Only `COMPLETE` games count toward "games played"; in-progress games do not.

Raw facts (attempts per round, clues revealed) are stored so streaks, attempt distribution, or a scoring formula can be added later without a migration. They are not shown now.
