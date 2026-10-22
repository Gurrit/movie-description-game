# Migration Plan: current repo → target stack

Snapshot of the current repo (at the time of writing):

| Area | Current | Target |
|---|---|---|
| Backend DB | Spring Data MongoDB (`db/repository/mongo`, entities `Challenge/Session/Score`) | PostgreSQL, R2DBC + Flyway |
| Backend model | 5 movies/day, sessions, per-clue point costs (`ClueCost`), scores | 7 movies/day, token-identified players, games/rounds, **no scoring** |
| Backend structure | Layers: `api`, `service`, `db`, `tmdb`, `dto` | Feature packages (`game`, `challenge`, `player`, `clue`, `stats`, `tmdb`) |
| API contract | `backend/api/openapi.yaml` (session/challenge/clue/guess/admin) | Root `api/openapi.yaml`, new endpoints per `../rules/05-api.md` |
| Frontend | CRA (`react-scripts`), yarn, `.js/.jsx`, Bootstrap, axios-generated client | Vite, pnpm, TypeScript, Tailwind, TanStack Query, `openapi-fetch` |
| Infra | `docker-compose.yml` (Mongo, mongo-express, dev frontend), nginx prod Dockerfile, `backend/k8s` (+Redis) | compose (dev) + `deploy/` with Caddy (prod) |

The working tree has uncommitted work in progress (a partial refactor of the backend controllers/DTOs). **Step 0 settles that before anything else.**

## Principles
- Small, reviewable steps; each phase ends with a green build and a working compose stack.
- Contract first: change `api/openapi.yaml` before code, on both sides.
- Reuse what is still valid: the TMDB clients, error handling, the OpenAPI generation setup, Dockerfile skeletons. Rewrite domain/persistence rather than adapting Mongo-shaped code.
- The old game has never been deployed with real users, so **no data migration** is needed. Mongo data is discarded.

## Phase 0: Prepare
1. Decide what to do with the uncommitted changes: commit them on a branch (`wip-refactor`) or discard what the plan replaces. Then create `migration/postgres-rewrite` from a clean state.
2. Tag the current `master` as `legacy-mongo` for reference.
3. Move specification into the repo (already in `specification/`); commit.

## Phase 1: Contract
1. `git mv backend/api/openapi.yaml api/openapi.yaml`; update the path in `backend/build.gradle.kts` and `app/package.json`.
2. Rewrite the spec for the new model: `servers: /api`, `bearerAuth`, endpoints in `../rules/05-api.md`, `problem+json` errors, schemas `Game`, `Round`, `Clue`, `GuessRequest/Response`, `MovieSuggestion`, `Stats`, `Player`. Drop `Session`, `Admin`, the clue-request/cost schemas, and scoring fields.
3. Put the internal endpoint in a separate `api/internal.yaml` (or tag-filtered) so the frontend types exclude it.
4. Add Spectral lint config and a CI step.
**Done when:** the spec lints; both generators run (they may fail to compile against old code, which is expected).

**Status: done.** `api/openapi.yaml` and `api/internal.yaml` written; Spectral config added; `backend/build.gradle.kts` points at `../api/openapi.yaml` with `useCoroutines=true` (generated interfaces are `suspend` functions returning `ResponseEntity<T>`); `app/package.json` `generate:api` path updated. Spike result: the kotlin-spring and openapi-typescript generators both handle the contract. Clue content is a single flat `ClueContent` object (optional `text`, `genres`, `runtimeMinutes`, `releaseYear`, `people`) rather than a `oneOf`, to keep generated code simple.

## Phase 2: Backend foundations
1. Gradle: remove `spring-boot-starter-data-mongodb`; add `data-r2dbc`, `r2dbc-postgresql`, Flyway (`flyway-core`, `flyway-database-postgresql`, `postgresql` JDBC), Actuator, Jackson Kotlin module. Pin versions via the Spring Boot BOM instead of inline versions where possible.
2. Switch the generator options to `useCoroutines=true` (keeps `reactive=true`, `interfaceOnly=true`).
3. Add `V1__init.sql` from `03-database.md`; add Postgres to `docker-compose.yml` (volume), remove `mongo` and `mongo-express`.
4. Add `@ConfigurationProperties` (`game`, `tmdb`, `generation`, `redaction`), an injectable `Clock`, and problem+json error handling (adapt `ErrorHandlerController`).
5. Delete: `db/repository/mongo`, Mongo entities, `ClueCost`, `AdminService`/`AdminController`, `ScoreRepository`/`ScoreEntity`, `SessionService`.
6. Add Testcontainers + a smoke integration test: context starts, migrations apply.
**Done when:** `./gradlew build` is green with an empty-but-wired app against Postgres.

**Status: done.**
- Dependencies are BOM-managed (`spring-boot-dependencies:4.0.1`). Boot 4 uses modular starters (`starter-flyway`, `starter-webflux-test`, `spring-boot-testcontainers`) and Jackson 3 (`tools.jackson.module:jackson-module-kotlin`).
- All old code was deleted, including the whole `tmdb/` package: it was blocking (`.block()`) and entangled with Mongo entities, so it is rewritten in phase 3 rather than adapted.
- Added: `application.yml`, validated `@ConfigurationProperties` (`game`, `tmdb`, `generation`, `redaction`), UTC `Clock`, `ApiException` + `ProblemExceptionHandler` (problem+json using the generated `Problem` model), `V1__init.sql`, a Postgres service in `docker-compose.yml` (Mongo and mongo-express removed).
- Health endpoints: Actuator's health is mapped to `/healthz` (not under `/api`): `/healthz/liveness`, `/healthz/readiness` (includes R2DBC).
- Tests: `ApplicationSmokeTest` (Testcontainers `postgres:17-alpine`) verifies that migrations create all 7 tables and that the probes are up. `./gradlew build` passes.
- Not yet done: the backend `Dockerfile` still assumes the old layout (copies `api/` from inside `backend/`). It is fixed in phase 5, when the build context moves to the repo root.

## Phase 3: Backend domain (in this order)
1. **tmdb/**: keep the existing clients; reshape to the three needed calls (discover, details+credits, search); add timeouts/retries; configurable base URL; WireMock tests with fixtures (≥ 7 movies).
2. **clue/**: sealed `Clue` model, `ClueBuilder` (from details + credits), `Redactor` (pure, unit-tested thoroughly).
3. **challenge/**: `MovieSelector` (seeded `Random`), `GameGenerator.ensure(date)` with advisory lock + no-repeat window (`generation.noRepeatDays`), `@Scheduled` job, `POST /internal/generate` with token check.
4. **player/**: `POST /players`, token hashing, bearer `WebFilter`.
5. **game/**: start, get, reveal (`RevealPolicy` seam), guess (single transaction), round advance, completion; `GameViewMapper` as the sole exposure point for clue content/answers.
6. **tmdb search endpoint** `GET /movies/search` with rate limiting + cache.
7. **stats/**: aggregate query + endpoint.
8. Replace `ApiContractTests` with spec-validating integration tests; port `SuggestionServiceTests` only if the suggestion logic survives (otherwise delete).
**Done when:** the full game flow passes integration tests end-to-end through the API with TMDB stubbed.

## Phase 4: Frontend rebuild
Rewrite instead of porting: the old components assume scoring/sessions/5 movies.
1. Replace CRA with Vite: new `app/` scaffold (`pnpm create vite` react-ts), `packageManager` pinned, delete `yarn.lock`, `react-scripts`, `reportWebVitals`, `src/api/generated/`, `nginx.conf`.
2. Add Tailwind, TanStack Query, React Router, `openapi-typescript`/`openapi-fetch`; `pnpm generate:api` against `../api/openapi.yaml`; Vite `server.proxy` for `/api`.
3. Build in order: token bootstrap + client middleware → `useGame` + start screen → round view with clue list/reveal → guess autocomplete → round result/next → game summary → stats page.
4. Salvage: layout ideas and any reusable presentational bits (`Loading`, `Error`, `ProgressIndicator`, `MovieCard`) can be re-created in TS/Tailwind; do not keep Bootstrap.
5. Vitest + Testing Library + MSW tests for the pieces listed in `06-testing.md`.
**Done when:** a full game is playable against the local backend in compose.

## Phase 5: Containers, compose and e2e
1. Backend Dockerfile: multi-stage Gradle → JRE 21, non-root, JVM flags per `07-deployment.md`.
2. Frontend: dev uses Vite in compose (or on host); `app/Dockerfile` builds `dist/` into a Caddy-based web image (replaces `prod.Dockerfile`, `nginx.conf`, `Dockerfile.dev`).
3. `docker-compose.yml`: `postgres`, `backend`, `app`, profile `e2e` adds `wiremock` and a pinned date.
4. Playwright e2e per `06-testing.md`.
5. Remove `backend/k8s/` (Redis and k8s are not part of the target).
**Done when:** `docker compose up` gives a working game; e2e passes.

## Phase 6: CI
GitHub Actions: lint/spec lint → backend tests → frontend typecheck/tests → e2e. Add Dependabot/Renovate config. Image build/push to GHCR on `master`.

## Phase 7: Deployment (see `07-deployment.md`)
1. Buy the domain; choose the VPS provider and size (≥ 2 GB RAM); point DNS.
2. Harden the host; install Docker; create the deploy user.
3. Add `deploy/` (prod compose, Caddyfile, `.env.example`, README with backup/restore); configure the GitHub secrets (SSH key, host); add the deploy job.
4. Set up nightly `pg_dump` to object storage; **do one test restore**.
5. First deploy, manually trigger `/internal/generate`, play a full game on the live URL, verify the next-midnight scheduled run.

## Phase 8: Cleanup
Update `README.md` (setup, commands, architecture pointer), remove dead files and dependencies, delete `legacy-mongo` references, and merge to `master`.

## Risks and mitigations
| Risk | Mitigation |
|---|---|
| Kotlin generator + coroutines produce awkward signatures | Spike in Phase 1 with one endpoint; adjust generator options before writing all controllers |
| R2DBC lacks JSONB convenience | Small custom converters (`Json` ↔ sealed `Clue`); covered by integration tests |
| Spring Boot 4.x / Kotlin 2.3 library compat (Testcontainers, validators, Flyway) | Check versions early in Phase 2; pin; fall back to the latest compatible release |
| TMDB pool/quality assumptions wrong | Build a small CLI/test that runs the selector against real TMDB once (manually, with a real key) and prints candidates + rejections; tune `minVoteCount`/`maxPoolPages` |
| Scope creep from old features | The functional spec's out-of-scope list governs; do not port scoring, admin or sessions |

## Suggested order summary
0 prepare → 1 contract → 2 backend foundations → 3 backend domain → 4 frontend → 5 containers + e2e → 6 CI → 7 deploy → 8 cleanup. Phases 3 and 4 can run in parallel once Phase 1 is done, since the frontend can develop against MSW mocks generated from the contract.
