# Technical Specification

Companion to the functional spec in `../rules/`. It covers how the system is built; the functional behavior is defined there.

| File | Contents |
|---|---|
| [01-architecture.md](01-architecture.md) | Stack, repo layout, backend and frontend structure |
| [02-api-contract.md](02-api-contract.md) | OpenAPI workflow, code generation, error format, internal endpoint |
| [03-database.md](03-database.md) | Postgres schema, migrations, R2DBC conventions |
| [04-backend.md](04-backend.md) | Packages, coroutines, TMDB client, generation, concurrency |
| [05-frontend.md](05-frontend.md) | Vite/pnpm, Tailwind, TanStack Query, routing, token handling |
| [06-testing.md](06-testing.md) | Test strategy per layer |
| [07-deployment.md](07-deployment.md) | VPS + compose + Caddy deployment, CI/CD, scheduler, backups |
| [08-migration-plan.md](08-migration-plan.md) | Phased plan from the current repo to this target |

## Decisions at a glance

| Area | Decision |
|---|---|
| Backend | Kotlin, Spring Boot (WebFlux), Kotlin coroutines (`suspend` / `Flow`) |
| Database | PostgreSQL via Spring Data R2DBC, Flyway migrations, JSONB for clue payloads |
| API | Spec-first OpenAPI; Kotlin server interfaces generated (kotlin-spring, reactive + coroutines); TS types via `openapi-typescript`, calls via `openapi-fetch` |
| Frontend | TypeScript, React, Vite, pnpm, Tailwind CSS, TanStack Query, React Router (`/`, `/stats`) |
| State | Server is the single source of truth for game state; only the player token is stored client-side |
| Scheduler | In-process `@Scheduled` at 00:00 UTC (advisory-lock guarded), lazy generation fallback, secured internal endpoint for manual runs |
| Tests | JUnit 5 + Testcontainers Postgres + WireMock (TMDB); Vitest + Testing Library; Playwright E2E on docker-compose; OpenAPI contract validation |
| Local dev | docker-compose only |
| Deployment | Single small VPS with docker-compose + Caddy; images built by GitHub Actions and pushed to GHCR; nightly off-box `pg_dump`. Portable to managed platforms later |
