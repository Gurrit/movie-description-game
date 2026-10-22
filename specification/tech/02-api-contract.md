# API Contract

## Workflow (spec-first)
1. Edit `api/openapi.yaml` (OpenAPI 3.1 or 3.0.x, whichever both generators support cleanly).
2. **Backend**: the openapi-generator `kotlin-spring` generator produces *interfaces* and *models* only, with `reactive=true` and `useCoroutines=true`, `interfaceOnly=true`, `useTags=true`. Controllers implement the generated interfaces. A change in the spec that breaks an implementation fails compilation.
3. **Frontend**: `openapi-typescript api/openapi.yaml -o src/api/schema.d.ts` (pnpm script `generate:api`); calls go through `openapi-fetch` with a thin middleware that injects `Authorization: Bearer <token>` and handles 401.
4. CI checks: spec lint (e.g. Spectral), backend build, frontend typecheck.

## Conventions
- JSON, camelCase. IDs: `tmdbId` as integer; player/game IDs as UUIDs.
- Dates: `date` fields use `YYYY-MM-DD` (UTC day); timestamps use `date-time`.
- Enums are closed in the spec but clients must tolerate unknown values for `ClueType`.
- Errors use RFC 9457 `application/problem+json`: `{type, title, status, detail, code}` where `code` is a stable machine-readable string (e.g. `ROUND_OVER`, `ALREADY_GUESSED`, `GAME_NOT_STARTED`, `TMDB_UNAVAILABLE`).
- Status codes: 200/201 success, 401 missing/invalid token, 404 unknown resource or game not started, 409 state conflicts (round over, duplicate guess), 422 validation, 429 rate limit, 503 generation impossible/TMDB down.
- Security scheme: `bearerAuth` (opaque token) for all public endpoints, except `POST /players`.

## Endpoints
Defined behavior is in `../rules/05-api.md`. Additions at technical level:
- `POST /internal/generate?date=YYYY-MM-DD` (optional `date`, default today UTC; **future dates ≤ `generation.preGenerateDays` only**). Auth: header `X-Internal-Token` compared in constant time with env `INTERNAL_TOKEN`. Idempotent: returns 200 with the existing set if present. It is **excluded from the public client types** (tagged `internal`, kept in a separate `api/internal.yaml` or filtered).
- `GET /healthz` (liveness) and `GET /readyz` (DB reachable), unauthenticated, outside the `/api` contract; use Spring Actuator health groups.

## Rate limiting
- Applied at the edge if available; otherwise, in-process token bucket per IP for `POST /players` and `GET /movies/search` (e.g. Bucket4j). Behind a proxy, use `X-Forwarded-For` (configure Spring `forward-headers-strategy`).
