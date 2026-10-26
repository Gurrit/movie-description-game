# Testing

| Layer | Tools | What is covered |
|---|---|---|
| Backend unit | JUnit 5, plain assertions | `Redactor`, `ClueBuilder`, `MovieSelector` (seeded random, fake TMDB), `RateLimitFilter` |
| Backend TMDB client | JUnit 5 + **WireMock** | Request parameters and auth header, parsing, 404 → null, retry on 5xx, no retry on 4xx |
| Backend integration | `@SpringBootTest` + WebTestClient, **Testcontainers Postgres**, **WireMock** for TMDB, fixed `Clock` | Full flows over HTTP: start, guess (unlocking clues), round advance, completion, stats, idempotency, concurrent double-submit, shared game, search, internal endpoint auth |
| Contract | `swagger-request-validator-core` against `api/openapi.yaml` | Every request/response of the integration flow is validated, so the server cannot drift from the spec |
| Frontend | Vitest, Testing Library, **MSW** with an in-memory fake of the API | A whole game through the UI, reload resume, error states, 401 recovery, autocomplete, clue rendering |
| End-to-end | **Playwright** against the real stack in docker-compose (`e2e/`): Postgres, backend, web image, and a **fake TMDB** | Full game in a browser, reload resumes, same game for every player, token recovery, proxy rules, deep links, phone layout |

## Rules
- TMDB is never called for real in any automated test. Backend tests stub it with WireMock; the e2e stack uses `e2e/fake-tmdb.mjs`, a ~80-line Node server with 200 deterministic movies ("Movie 1 Title" … "Movie 200 Title") that also serves placeholder posters. The backend finds it through `TMDB_BASE_URL`.
- Backend tests control time with the injected `Clock` (fixed). The e2e stack uses the real UTC date; it needs no date pinning because every run starts from an empty database.
- The e2e stack uses its own compose project (`mdg-e2e`) and ports (Postgres 5433, web 8082, fake TMDB 8089) so it can run next to the development stack. Its Postgres is on tmpfs. The backend there runs a short game (`GAME_ROUNDSPERGAME=3`, `GAME_ATTEMPTSPERROUND=2`) to keep tests quick.
- The API never reveals answers, so e2e tests read the day's answers from Postgres (`pg`) and play them through the UI.
- `pnpm test` in `e2e/` builds and starts the stack, waits for every container to be healthy, runs the tests in a desktop and a phone (Pixel 7) project, and tears the stack down. `E2E_KEEP=1` leaves it running.
- Environment variable names for Spring properties drop the dashes: `game.rounds-per-game` is `GAME_ROUNDSPERGAME`.
- CI order: lint/spec-lint → backend build + tests → frontend typecheck, lint, tests → e2e.
