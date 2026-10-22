CREATE TABLE player (
  id           uuid PRIMARY KEY,
  token_hash   bytea NOT NULL UNIQUE,
  created_at   timestamptz NOT NULL,
  last_seen_at timestamptz NOT NULL
);

CREATE TABLE movie (
  id             integer PRIMARY KEY,
  title          text NOT NULL,
  original_title text NOT NULL,
  release_year   smallint NOT NULL,
  poster_path    text,
  clues          jsonb NOT NULL
);

-- A game: a fixed list of movies plus the rules it was generated with. Shared by all players.
CREATE TABLE game (
  id                 uuid PRIMARY KEY,
  rounds_per_game    smallint NOT NULL,
  attempts_per_round smallint NOT NULL,
  created_at         timestamptz NOT NULL
);

-- Which game is played on which UTC day. "One game per date" is enforced by the
-- generator (under an advisory lock), deliberately not by a constraint.
CREATE TABLE daily_game (
  game_id uuid PRIMARY KEY REFERENCES game (id),
  date    date NOT NULL
);

CREATE INDEX daily_game_date_idx ON daily_game (date);

CREATE TABLE game_movie (
  game_id  uuid NOT NULL REFERENCES game (id),
  position smallint NOT NULL,
  movie_id integer NOT NULL REFERENCES movie (id),
  PRIMARY KEY (game_id, position),
  UNIQUE (game_id, movie_id)
);

CREATE INDEX game_movie_movie_idx ON game_movie (movie_id);

-- One player's run through a game.
CREATE TABLE playthrough (
  id            uuid PRIMARY KEY,
  player_id     uuid NOT NULL REFERENCES player (id),
  game_id       uuid NOT NULL REFERENCES game (id),
  status        text NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETE')),
  current_round smallint NOT NULL,
  started_at    timestamptz NOT NULL,
  completed_at  timestamptz,
  UNIQUE (player_id, game_id)
);

CREATE TABLE playthrough_round (
  playthrough_id uuid NOT NULL REFERENCES playthrough (id),
  position       smallint NOT NULL,
  status         text NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SOLVED', 'FAILED')),
  PRIMARY KEY (playthrough_id, position)
);

CREATE TABLE guess (
  playthrough_id uuid NOT NULL,
  position       smallint NOT NULL,
  attempt_no     smallint NOT NULL,
  tmdb_id        integer NOT NULL,
  title          text NOT NULL,
  release_year   smallint,
  correct        boolean NOT NULL,
  created_at     timestamptz NOT NULL,
  PRIMARY KEY (playthrough_id, position, attempt_no),
  UNIQUE (playthrough_id, position, tmdb_id),
  FOREIGN KEY (playthrough_id, position) REFERENCES playthrough_round (playthrough_id, position)
);

CREATE TABLE revealed_clue (
  playthrough_id          uuid NOT NULL,
  position                smallint NOT NULL,
  clue_id                 text NOT NULL,
  revealed_at             timestamptz NOT NULL,
  attempts_used_at_reveal smallint NOT NULL,
  PRIMARY KEY (playthrough_id, position, clue_id),
  FOREIGN KEY (playthrough_id, position) REFERENCES playthrough_round (playthrough_id, position)
);

CREATE INDEX playthrough_player_idx ON playthrough (player_id);
