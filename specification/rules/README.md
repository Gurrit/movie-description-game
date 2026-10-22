# Movie Description Game: Specification

A daily game where players identify movies from clues.

| File | Contents |
|---|---|
| [01-game-rules.md](01-game-rules.md) | Game, round, attempt and clue-reveal rules |
| [02-clues.md](02-clues.md) | Clue types, data sources, redaction, generic clue model |
| [03-challenge-generation.md](03-challenge-generation.md) | Daily job and TMDB query strategy |
| [04-players-and-stats.md](04-players-and-stats.md) | Anonymous token sessions, game persistence, stats |
| [05-api.md](05-api.md) | Backend API surface (to be expressed in `../../backend/api/openapi.yaml`) |
| [06-configuration.md](06-configuration.md) | Config file keys and defaults |
| [07-out-of-scope.md](07-out-of-scope.md) | Explicit non-goals and open items |

## Glossary

- **Daily set**: the ordered list of 7 distinct movies generated once per UTC day, identical for all players.
- **Game**: one player's playthrough of one daily set. One game per player per day.
- **Round**: one movie within a game. A game has 7 rounds, played sequentially.
- **Attempt**: one guess at the movie in the current round. A round allows 3 attempts, which do not carry over between rounds.
- **Clue**: a piece of information about the round's movie, revealed on request.
- **Player**: an anonymous identity represented by a backend-issued opaque token.

## Stack (existing)

See [../tech/](../tech/README.md): Kotlin / Spring Boot WebFlux with PostgreSQL; React + Vite + pnpm frontend; contract-first OpenAPI (`api/openapi.yaml`).
