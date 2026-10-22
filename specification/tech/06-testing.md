# Testing

| Layer | Tools | What is covered |
|---|---|---|
| Backend unit | JUnit 5, Kotest assertions or AssertJ, MockK | `Redactor`, clue building, `MovieSelector` (seeded random), reveal policy, game state transitions |
| Backend integration | `@SpringBootTest` + WebTestClient, **Testcontainers Postgres**, **WireMock** for TMDB | Full flows: start game, reveal, guess, round advance, completion, stats, idempotency, concurrent double-submit, lazy generation, internal endpoint auth, migrations apply cleanly |
| Contract | Validate every integration-test response against `api/openapi.yaml` (e.g. `swagger-request-validator` for WebTestClient or an equivalent) | Server never drifts from the spec; error responses match `problem+json` |
| Frontend unit/component | Vitest, React Testing Library, MSW for API mocks | Autocomplete behavior, round rendering from API state, 401 recovery, token storage failures |
| End-to-end | Playwright against docker-compose (backend, Postgres, frontend, **WireMock as TMDB**) | One full happy-path game, reload mid-game resumes, finished game is read-only, failed round shows the answer |

## Rules
- TMDB is never called for real in any automated test. WireMock stubs live in `backend/src/test/resources/wiremock` and are reused by the e2e compose profile through the configurable `tmdb.baseUrl`.
- Time is controlled through the injected `Clock`; e2e uses a test profile that allows pinning the date.
- Test data needs a fixed set of ≥ 7 valid TMDB movie fixtures (with credits).
- CI order: lint/spec-lint → backend build + tests → frontend typecheck + tests → e2e.
