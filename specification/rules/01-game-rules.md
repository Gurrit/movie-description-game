# Game Rules

## Structure
- A **daily set** contains `roundsPerGame` movies (default **7**), all distinct.
- The **game** for a day is the same for every player. A player's run through it is their **playthrough**. Each player may play **one playthrough per UTC day**. There is no restart, and no way to play past days.
- Rounds are **sequential**: the player must finish round N (solved or failed) before round N+1 is visible. No skipping, no returning to earlier rounds for further play.

## Round
- Each round allows `attemptsPerRound` attempts (default **3**). Attempts never carry over to the next round.
- The round starts with **no clues revealed**. The player may reveal any available clue at any time (see [02-clues.md](02-clues.md)).
- **Revealing a clue has no effect on the game loop**: it does not consume an attempt and does not affect the result (scoring is out of scope). The model must still record *which* clues were revealed and *when*, so a later rule such as "a reveal costs an attempt" or "one clue per attempt" can be added without a data migration.
- **Guessing**: the player picks a movie from a TMDB-backed autocomplete (title + release year shown). A guess is correct iff the guessed TMDB movie id equals the round's movie id. No title matching or fuzzy matching.
- Round outcomes:
  - **Solved**: a correct guess within the allowed attempts. The round ends immediately.
  - **Failed**: `attemptsPerRound` wrong guesses. The round ends.
- When a round ends (either way), the server reveals the answer (title, year, poster) and the player proceeds to the next round.
- The same movie may not be guessed twice in a round (the UI should disable it, and the server rejects it without consuming an attempt).

## Game completion
- A game is **complete** after the last round ends. It then shows a summary: per round, the movie, solved/failed, attempts used, and clues revealed.
- Reloading mid-game restores the exact state (current round, attempts used, revealed clues). Reloading after completion shows the read-only summary.

## Integrity rules
- Server-authoritative: the answer and unrevealed clue content never leave the server before they are earned (revealed clue or round ended).
- Clients cannot start a game for a day other than the current UTC day.
- A player can have at most one playthrough per game, hence per UTC day (enforced by a unique constraint on player and game).
