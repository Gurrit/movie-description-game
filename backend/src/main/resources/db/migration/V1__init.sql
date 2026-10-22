CREATE TABLE players (
  id           uuid PRIMARY KEY,
  token_hash   bytea NOT NULL UNIQUE,
  created_at   timestamptz NOT NULL,
  last_seen_at timestamptz NOT NULL
);

CREATE TABLE daily_sets (
  date        date PRIMARY KEY,
  created_at  timestamptz NOT NULL
);

CREATE TABLE daily_set_movies (
  date           date NOT NULL REFERENCES daily_sets(date),
  position       smallint NOT NULL,
  tmdb_id        integer NOT NULL,
  title          text NOT NULL,
  original_title text NOT NULL,
  release_year   smallint NOT NULL,
  poster_path    text,
  clues          jsonb NOT NULL,
  PRIMARY KEY (date, position),
  UNIQUE (tmdb_id)
);

CREATE TABLE games (
  id                 uuid PRIMARY KEY,
  player_id          uuid NOT NULL REFERENCES players(id),
  date               date NOT NULL REFERENCES daily_sets(date),
  status             text NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETE')),
  rounds_per_game    smallint NOT NULL,
  attempts_per_round smallint NOT NULL,
  current_round      smallint NOT NULL,
  started_at         timestamptz NOT NULL,
  completed_at       timestamptz,
  UNIQUE (player_id, date)
);

CREATE TABLE game_rounds (
  game_id  uuid NOT NULL REFERENCES games(id),
  position smallint NOT NULL,
  status   text NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SOLVED', 'FAILED')),
  PRIMARY KEY (game_id, position)
);

CREATE TABLE guesses (
  game_id    uuid NOT NULL,
  position   smallint NOT NULL,
  attempt_no smallint NOT NULL,
  tmdb_id    integer NOT NULL,
  correct    boolean NOT NULL,
  created_at timestamptz NOT NULL,
  PRIMARY KEY (game_id, position, attempt_no),
  UNIQUE (game_id, position, tmdb_id),
  FOREIGN KEY (game_id, position) REFERENCES game_rounds (game_id, position)
);

CREATE TABLE revealed_clues (
  game_id                 uuid NOT NULL,
  position                smallint NOT NULL,
  clue_id                 text NOT NULL,
  revealed_at             timestamptz NOT NULL,
  attempts_used_at_reveal smallint NOT NULL,
  PRIMARY KEY (game_id, position, clue_id),
  FOREIGN KEY (game_id, position) REFERENCES game_rounds (game_id, position)
);

CREATE INDEX games_player_idx ON games (player_id);
