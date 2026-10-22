# Architecture

```
Browser (React SPA) ──/api/*──▶ Backend (Spring WebFlux) ──R2DBC──▶ PostgreSQL
                                     │
                                     └──HTTPS──▶ TMDB API
Caddy (TLS, serves the SPA, proxies /api) sits in front; the backend generates the daily set via @Scheduled at 00:00 UTC
```

## Repository layout (monorepo)
```
api/openapi.yaml        # single source of truth for the HTTP contract (moved from backend/api/)
app/                    # frontend (Vite + React + TS), managed by pnpm
backend/                # Kotlin / Spring Boot, Gradle (Kotlin DSL)
docker-compose.yml      # local dev: postgres, backend, app, (wiremock for e2e)
specification/          # this documentation
```
- Both `backend/` and `app/` read `api/openapi.yaml`; neither owns it.
- Generated code is **not committed** (generated in the build). Generated output goes to `backend/build/generated` and `app/src/api/schema.d.ts` (git-ignored).

## Runtime shape
- Backend and frontend are separate deployables. In every environment the browser sees a **single origin**: a reverse proxy or the host's rewrite rules forward `/api/*` to the backend (dev: Vite proxy; compose: same). Therefore no CORS config is needed. If a deployment can't proxy, enable CORS through an env-configured allowed origin (`APP_CORS_ORIGIN`).
- All API paths are under the `/api` prefix (OpenAPI `servers: - url: /api`).
- Backend is stateless: all state in Postgres, so multiple instances are safe.

## Time
- "Today" is always the UTC date, computed on the server through an injected `Clock`. The client never decides the date.
- Timestamps in the DB are `timestamptz`; API timestamps are ISO-8601 UTC.
