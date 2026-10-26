# Movie Description Game

A daily game: every UTC day there is one game (a set of movies, default 7, the same for every player). A player starts each movie with one clue and every wrong guess unlocks the next (details, writer, studio, director, top billing, tagline, plot), or skip to the next clue at the cost of an attempt (default 7 attempts, one per clue). Personal stats only; no scoring, no accounts.

**The specification is the source of truth for behaviour and design. Read it before changing anything non-trivial:**
- `specification/rules/` is what the game does (rules, clues, TMDB selection, players, config).
- `specification/tech/` is how it is built (architecture, database with ER diagram, backend, frontend, testing, deployment, migration plan with status per phase).

If you change behaviour or a design decision, update the matching spec file in the same change.

## Layout

| Path | |
|---|---|
| `api/openapi.yaml` | The HTTP contract. Source of truth for backend and frontend. `api/internal.yaml` documents the operational endpoint (docs only, not generated). |
| `backend/` | Kotlin, Spring Boot 4 (WebFlux), coroutines, R2DBC + Flyway, PostgreSQL. Gradle. |
| `app/` | React + TypeScript SPA: Vite, pnpm, Tailwind, TanStack Query, React Router. |
| `e2e/` | Playwright against the whole stack in its own docker-compose project, with a fake TMDB. |
| `specification/` | The docs described above. |
| `.claude/skills/`, `apm.yml`, `apm.lock.yaml` | Agent skills, managed by APM. Change them with `apm install`, never by hand. |

## Commands

```sh
cd backend && ./gradlew test        # unit + integration tests; needs Docker (Testcontainers)
cd backend && ./gradlew build       # also generates the API interfaces from ../api/openapi.yaml
cd app && pnpm test                 # Vitest + Testing Library + MSW
cd app && pnpm lint && pnpm typecheck && pnpm build
cd e2e && pnpm test                 # builds and starts the stack, runs Playwright, tears down; needs Docker + buildx
docker compose up -d postgres backend    # dev stack (needs TMDB_READ_TOKEN in .env)
```

Run the relevant tests before finishing. The backend and frontend tests are quick; run `e2e` when you change behaviour that crosses the API, the proxy or the UI.

## Rules that are easy to get wrong

**Contract first.** Change `api/openapi.yaml` first, then the code. Backend interfaces and models are generated into `backend/build/generated` and the frontend's `app/src/api/schema.d.ts` is generated and git-ignored: never edit generated code. Every response in the backend integration test is validated against the spec, so drift fails the build. Responses omit `null` fields (`default-property-inclusion: non_null`) and the contract relies on it.

**The server is authoritative.** The answer and the content of locked clues must never reach the client early. `game/GameViewMapper` is the only place that decides what a player may see; keep it that way. The frontend has no game rules, it renders what the API returns.

**Backend style.**
- `suspend` functions, no blocking calls on the event loop. Feature packages (`game`, `challenge`, `player`, `clue`, `tmdb`, `stats`, `movie`), not technical layers.
- Persistence is `DatabaseClient` with plain SQL in small repository classes. No ORM, no JPA advice applies here.
- Table names are singular; REST paths are plural (`POST /players`).
- Errors are `ApiException(status, code, title, detail)` rendered as `application/problem+json` with a stable `code`.
- "Today" is always the UTC date from the injected `Clock`. Never call `LocalDate.now()` without it.
- Config lives in `application.yml` as validated `@ConfigurationProperties`. Env var names drop the dashes (`game.rounds-per-game` is `GAME_ROUNDSPERGAME`).
- Schema changes are new Flyway migrations. `V1__init.sql` was edited in place only because nothing had been deployed; do not do that once a database exists.
- One game per date and the no-repeat window for movies are enforced in code (advisory lock in `GameGenerator`), deliberately not by database constraints. Guess and clue primary keys stay as constraints because they protect against double submits.

**Frontend style.** Clean, mobile-first, Wordle-like, a narrow centred column (`max-w-xl`). The visual direction is decided in `specification/tech/05-frontend.md`; the `frontend-design` skill must follow that brief rather than pick a new look. The guess box and primary action are pinned to the bottom on phones (`BottomBar`). TypeScript is pinned to 5.9.x because `openapi-typescript` and `typescript-eslint` need its JavaScript API.

**Tests never call the real TMDB.** Backend tests stub it with WireMock, the e2e stack uses `e2e/fake-tmdb.mjs`. The API never reveals answers, so e2e tests read them from Postgres.

## Gotchas

- `spring.webflux.base-path` is `/api`, and that includes Actuator: health is `/api/healthz/liveness` and `/api/healthz/readiness`. In tests, `WebTestClient` already prefixes `/api`.
- The Docker builds (`backend/Dockerfile`, `app/Dockerfile`) use the **repository root as context** (they need `api/`) and BuildKit cache mounts, so `docker buildx` is required.
- The e2e stack uses its own ports (Postgres 5433, web 8082, fake TMDB 8089) so it can run next to the dev stack.
- When stubbing the API in a browser with Playwright's `page.route`, match on `pathname.startsWith('/api/')`, not `**/api/**`, which also catches Vite's `/src/api/*` modules.
- MSW 3 fails on unmatched requests with `onUnhandledFrame: 'error'` (the old `onUnhandledRequest` is silently ignored).

## Working here

- Commit messages: an imperative subject, then a body that says why, not what.
- Real credentials (the TMDB token, the Anthropic key) come from the environment or repository secrets. Never commit them, and never put them in test fixtures.
- Prefer small changes that leave `./gradlew test`, `pnpm test`, `pnpm lint` and `pnpm typecheck` green.
