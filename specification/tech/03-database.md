# Database (PostgreSQL)

- Access: Spring Data R2DBC (`r2dbc-postgresql`), repositories as coroutine `CoroutineCrudRepository`; `DatabaseClient` / custom queries where atomicity matters.
- Migrations: **Flyway**, plain SQL under `backend/src/main/resources/db/migration` (`V1__init.sql`, …). Flyway needs JDBC: include `flyway-database-postgresql` and the `postgresql` JDBC driver for startup migration only. Application queries remain R2DBC.
- The database is never purged. Data for each day is retained for ever.
- Primary keys: UUID (generated app-side, UUIDv4/v7) except where a natural key exists.

## Schema (initial)
```sql
CREATE TABLE players (
  id           uuid PRIMARY KEY,
  token_hash   bytea NOT NULL UNIQUE,          -- SHA-256 of the opaque token
  created_at   timestamptz NOT NULL,
  last_seen_at timestamptz NOT NULL
);

CREATE TABLE daily_sets (
  date        date PRIMARY KEY,                 -- UTC day
  created_at  timestamptz NOT NULL
);

CREATE TABLE daily_set_movies (
  date          date NOT NULL REFERENCES daily_sets(date),
  position      smallint NOT NULL,              -- 0-based round index
  tmdb_id       integer NOT NULL,
  title         text NOT NULL,
  original_title text NOT NULL,
  release_year  smallint NOT NULL,
  poster_path   text,
  clues         jsonb NOT NULL,                 -- ordered array of Clue (see ../rules/02-clues.md)
  PRIMARY KEY (date, position),
  UNIQUE (tmdb_id)                              -- a movie is never reused in any set
);

CREATE TABLE games (
  id                uuid PRIMARY KEY,
  player_id         uuid NOT NULL REFERENCES players(id),
  date              date NOT NULL REFERENCES daily_sets(date),
  status            text NOT NULL CHECK (status IN ('IN_PROGRESS','COMPLETE')),
  rounds_per_game   smallint NOT NULL,          -- config snapshot
  attempts_per_round smallint NOT NULL,         -- config snapshot
  current_round     smallint NOT NULL,
  started_at        timestamptz NOT NULL,
  completed_at      timestamptz,
  UNIQUE (player_id, date)
);

CREATE TABLE game_rounds (
  game_id    uuid NOT NULL REFERENCES games(id),
  position   smallint NOT NULL,
  status     text NOT NULL CHECK (status IN ('PENDING','ACTIVE','SOLVED','FAILED')),
  PRIMARY KEY (game_id, position)
);

CREATE TABLE guesses (
  game_id    uuid NOT NULL,
  position   smallint NOT NULL,
  attempt_no smallint NOT NULL,                 -- 1-based
  tmdb_id    integer NOT NULL,
  correct    boolean NOT NULL,
  created_at timestamptz NOT NULL,
  PRIMARY KEY (game_id, position, attempt_no),
  UNIQUE (game_id, position, tmdb_id),          -- no duplicate guess within a round
  FOREIGN KEY (game_id, position) REFERENCES game_rounds
);

CREATE TABLE revealed_clues (
  game_id    uuid NOT NULL,
  position   smallint NOT NULL,
  clue_id    text NOT NULL,
  revealed_at timestamptz NOT NULL,
  attempts_used_at_reveal smallint NOT NULL,
  PRIMARY KEY (game_id, position, clue_id),
  FOREIGN KEY (game_id, position) REFERENCES game_rounds
);
```
Notes:
- `UNIQUE (tmdb_id)` on `daily_set_movies` enforces the "never repeat" rule; generation retries on conflict.
- `guesses` primary key `(game_id, position, attempt_no)` makes concurrent double submits fail on insert instead of consuming two attempts; the service maps the unique-violation to `409`.
- Stats are an aggregate query over `games`/`game_rounds` (indexed by `player_id`). Counters are not stored initially.
- `clues` JSONB shape: `[{"id":"plot","type":"PLOT","order":1,"content":{...}}, ...]`; content shapes are documented per type in `../rules/02-clues.md`. The reader tolerates unknown fields. Content is deserialized with Jackson into a sealed Kotlin hierarchy.
- Round-ending, guess insert and game update happen in **one transaction** (`TransactionalOperator`); the round row is locked (`SELECT … FOR UPDATE`) while evaluating a guess.

## Local and test
- Local: Postgres container in compose with a named volume.
- Tests: Testcontainers (same Postgres major version as production, pinned).
