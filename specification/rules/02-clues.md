# Clues

## Clue types (initial)
All content comes from TMDB (`/movie/{id}` and `/movie/{id}/credits`). Each type is a *generic clue definition*; the UI lists them in this order but the player chooses freely.

| # | Type key | Content | Source |
|---|---|---|---|
| 1 | `PLOT` | Plot overview, spoiler-redacted | `overview` |
| 2 | `TAGLINE` | Tagline, redacted | `tagline` |
| 3 | `METADATA` | Genres, runtime, release year | `genres`, `runtime`, `release_date` |
| 4 | `PEOPLE` | The 3 first-billed cast members, the director and the writer (see below) | credits |

### PEOPLE details
- Cast: first 3 by `order` in credits (name and character).
- Director: crew with `job == "Director"` (first one if several).
- Writer: crew with `job` in (`Screenplay`, `Writer`, `Story`), in that preference order (first one).
- This clue is made of **multiple items** (3 cast, director, writer). For the initial version, the whole PEOPLE clue is revealed in one go. The model must nonetheless represent clues as a list of items so that per-item reveals (one actor at a time) can be added later.

## Generic clue model
```
Clue {
  id:        stable id within the round, e.g. "plot", "people.cast.0"
  type:      PLOT | TAGLINE | METADATA | PEOPLE | ...   (open enum)
  order:     display order
  content:   type-specific payload, built at generation time and stored with the challenge
}
RevealedClue { clueId, revealedAt, attemptsUsedAtReveal }
```
- Clues are **generated once when the challenge is created** and stored. They are never recomputed from TMDB at play time.
- The *reveal policy* (who may reveal what, and when) is isolated in a single component. Today the policy is "any clue, any time, free". Alternatives to remain easy to implement: "next clue revealed automatically per wrong attempt", "reveal costs an attempt".
- Clients ask to reveal a clue by id. The server returns the content, and records it.
- Rounds returned to clients list unrevealed clues only as `{id, type, order}` (a label and no content).

## Redaction (spoiler stripping)
Applied to `PLOT` and `TAGLINE` at generation time:
- Mask every occurrence of each token of the movie title (and original title) with `▇▇▇` (length-preserving not required), case- and punctuation-insensitive. Ignore trivial stopwords (`the`, `a`, `of`, ...) when masking, so "The" is not blanked everywhere. Also mask the full title as a phrase.
- Mask the name (full name and each name part longer than 2 chars) of every person in the PEOPLE clue, so the overview cannot give away the cast/director before the player reveals them.
- Franchise/sequel numbers are not specially handled.
- Edge case: if after redaction more than `redaction.maxMaskedRatio` (default 0.35) of the overview's words are masked, the movie is rejected during generation and another is drawn.
- `METADATA` and `PEOPLE` contain no redaction (they never contain the title).

## Required data
A movie is only eligible when it has: non-empty overview, non-empty tagline, at least 3 cast, a director and a writer.
