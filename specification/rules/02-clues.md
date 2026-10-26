# Clues

## Clue types
All content comes from TMDB (`/movie/{id}` with credits). The clues of a round unlock one at a time, in this order: the first is shown from the start, each wrong guess unlocks the next (see [01-game-rules.md](01-game-rules.md#round)).

| # | Type key | Id | Content | Source |
|---|---|---|---|---|
| 1 | `METADATA` | `metadata` | Genres, runtime, release **decade** (shown as "Details") | `genres`, `runtime`, `release_date` |
| 2 | `WRITER` | `writer` | The writer's name | credits |
| 3 | `STUDIO` | `studio` | The main production company | `production_companies[0]` |
| 4 | `DIRECTOR` | `director` | The director's name | credits |
| 5 | `CAST` | `cast` | The 3 first-billed actors (shown as "Top billing") | credits |
| 6 | `TAGLINE` | `tagline` | Tagline, redacted | `tagline` |
| 7 | `PLOT` | `plot` | Plot overview, spoiler-redacted | `overview` |

### Studio
- The first of TMDB's `production_companies`, as a single name.

### Details
- The stored clue keeps the exact release year, but the API only exposes the **decade** (`decade: 1990` is shown as "1990s"). The exact year would narrow the movie down too much for the first clue. `GameViewMapper` does the conversion.

### People
- Cast: first 3 by `order` in credits. **Only the actors' names are used, never the characters they play**: a character name often gives the movie away.
- Director: crew with `job == "Director"` (first one if several).
- Writer: crew with `job` in (`Screenplay`, `Writer`, `Story`), in that preference order (first one).
- Each is its own clue with a list of `names` (one name for writer and director, three for the cast).

## Generic clue model
```
Clue {
  id:        stable id within the round, e.g. "plot"
  type:      METADATA | WRITER | STUDIO | DIRECTOR | CAST | TAGLINE | PLOT | ...   (open enum)
  order:     position in the unlock order, starting at 1
  content:   type-specific payload, built at generation time and stored with the movie
}
```
- Clues are **generated once when the movie is first used** and stored. They are never recomputed from TMDB at play time.
- `order` is both the display order and the unlock order. A clue is unlocked when `order <= attempts in the round (wrong guesses and skips) + 1`, or when the round has ended. This rule lives in `GameViewMapper`, the only place that decides what a player may see.
- Rounds returned to clients list every clue as `{id, type, order, revealed}`; `content` is present only for unlocked clues, so locked ones are just a label.
- To change the order or add a clue type, change the order in `ClueBuilder`, and set `attemptsPerRound` to match the number of clues.

## Redaction (spoiler stripping)
Applied to `PLOT` and `TAGLINE` at generation time:
- Mask every occurrence of each token of the movie title (and original title) with `▇▇▇` (length-preserving not required), case- and punctuation-insensitive. Ignore trivial stopwords (`the`, `a`, `of`, ...) when masking, so "The" is not blanked everywhere. Also mask the full title as a phrase.
- Also mask the names of the writer, director and top-billed cast.
- Franchise/sequel numbers are not specially handled.
- Edge case: if after redaction more than `redaction.maxMaskedRatio` (default 0.35) of the overview's words are masked, the movie is rejected during generation and another is drawn.
- The other clues contain no redaction (they never contain the title).

## Required data
A movie is only eligible when it has: non-empty overview, non-empty tagline, at least 3 cast, a director, a writer and a production company.

Movies stored before the studio clue existed keep their six clues; the rule above works on `order`, so they play fine.
