# Movie Description Game

A daily game: every day there is a new set of movies. Reveal clues (plot, tagline, details, people), then name each movie in a few attempts.

Kotlin / Spring Boot (WebFlux) + PostgreSQL backend, React + TypeScript frontend, one OpenAPI contract in `api/openapi.yaml`.
The functional and technical specification is in [`specification/`](specification/README.md).

## Layout

| Directory | |
|---|---|
| `api/` | OpenAPI contract, the source of truth for both sides |
| `backend/` | Spring Boot application (Gradle) |
| `app/` | React SPA (Vite, pnpm) and its web image (Caddy) |
| `e2e/` | Playwright end-to-end tests with their own docker-compose stack and a fake TMDB |
| `specification/` | What the game does (`rules/`) and how it is built (`tech/`) |

## Develop

You need Docker, JDK 21+ and Node with pnpm. Get a TMDB "API Read Access Token" at https://www.themoviedb.org/settings/api, then:

```sh
cp .env.example .env                        # put your token in TMDB_READ_TOKEN
docker compose up -d postgres backend       # API on http://localhost:8080
cd app && pnpm install && pnpm dev          # app on http://localhost:3000 (proxies /api to :8080)
```

Or run the production-like stack (web image with Caddy in front of the backend) on http://localhost:8000:

```sh
docker compose --profile web up --build
```

## Test

```sh
cd backend && ./gradlew test                # unit + integration tests (needs Docker for Testcontainers)
cd app && pnpm test                         # component tests
cd e2e && pnpm install && pnpm exec playwright install chromium && pnpm test
                                            # builds and starts the whole stack with a fake TMDB, then drives it in a browser
```

## AI Disclosure
This codebase was in large part created using AI.
