# Game Rules

## Structure
- A **daily set** contains `roundsPerGame` movies (default **7**), all distinct.
- The **game** for a day is the same for every player. A player's run through it is their **playthrough**. Each player may play **one playthrough per UTC day**. There is no restart, and no way to play past days.
- Rounds are **sequential**: the player must finish round N (solved or failed) before round N+1 is visible. No skipping, no returning to earlier rounds for further play.

## Round
- Each round allows `attemptsPerRound` attempts (default **7**, one per clue). Attempts never carry over to the next round.
- The round starts with **one clue shown**, the movie's details. **Every wrong guess unlocks the next clue**, in a fixed order: details, writer, studio, director, top billing, tagline, plot (see [02-clues.md](02-clues.md)). With the default seven attempts the last guess is made with every clue unlocked.
- **Skip**: the player may give up an attempt without naming a movie. It counts as an attempt and unlocks the next clue exactly like a wrong guess, and skipping the last attempt fails the round. Skips are stored as a per-round counter.
- **Random**: the guess box has a button that fills in a random well-known movie (from the pool the games are built from, so it says nothing about the answer). It only fills the box; the player confirms it with the Guess button, and it then counts like any other guess.
- Which clues a player can see is derived from the number of attempts (guesses and skips) made in the round, so nothing about unlocking is stored.
- **Guessing**: the player picks a movie from a TMDB-backed autocomplete (title + release year shown). A guess is correct iff the guessed TMDB movie id equals the round's movie id. No title matching or fuzzy matching.
- Round outcomes:
  - **Solved**: a correct guess within the allowed attempts. The round ends immediately.
  - **Failed**: `attemptsPerRound` wrong guesses. The round ends.
- When a round ends (either way), the server reveals the answer (title, year, poster) and the player proceeds to the next round.
- The same movie may not be guessed twice in a round (the UI should disable it, and the server rejects it without consuming an attempt).

## Game completion
- A game is **complete** after the last round ends. It then shows a summary: per round, the movie, solved/failed and attempts used.
- Reloading mid-game restores the exact state (current round, attempts used, unlocked clues). Reloading after completion shows the read-only summary.

## Integrity rules
- Server-authoritative: the answer and the content of locked clues never leave the server before they are earned (unlocked by a wrong guess, or the round ended).
- Clients cannot start a game for a day other than the current UTC day.
- A player can have at most one playthrough per game, hence per UTC day (enforced by a unique constraint on player and game).
