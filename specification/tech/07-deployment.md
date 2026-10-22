# Deployment

**Decision:** a single small VPS running docker-compose, with Caddy as the reverse proxy and TLS terminator. Chosen for low cost (~€4-7/month), no cold starts, and because it reuses the local compose setup. Performance and high availability are explicitly not goals; traffic is expected to be very low.

## Topology
```
Internet ─443─▶ Caddy ─┬─ /api/*  ─▶ backend:8080 ─▶ postgres:5432 (internal network only)
                       └─ /*      ─▶ static files (Vite build, SPA fallback to index.html)
```
- One VPS (2 vCPU / 2 GB RAM is comfortable for JVM + Postgres + Caddy; 1 GB is tight, so avoid it). Any provider with cheap Linux VPSes works (e.g. Hetzner, or similar). Ubuntu/Debian LTS.
- Only ports 80/443 (and SSH, key-only) are open. Postgres and the backend are not published to the host network.
- A domain (to be bought, ~€10/year) with an A/AAAA record to the VPS. Caddy obtains and renews Let's Encrypt certificates automatically.

## Compose files
- `docker-compose.yml`: local development (Postgres, backend, Vite dev server).
- `deploy/docker-compose.prod.yml`: production services `caddy`, `backend`, `postgres`, using images from GHCR (`ghcr.io/<owner>/<repo>-backend`, `…-web`), with `restart: unless-stopped`, a named volume for Postgres data, healthchecks, and log rotation (`json-file` with `max-size`).
- `deploy/Caddyfile`: site block for the domain; `handle /api/* { reverse_proxy backend:8080 }`; everything else `file_server` with `try_files {path} /index.html`; gzip/zstd; security headers.
- The **web image** is `caddy` plus the built `dist/` (multi-stage build: node/pnpm build → copy into the caddy image). So the server needs only Docker; no Node or JDK on the host.
- `deploy/.env` (on the server only, not in git) holds secrets; `deploy/.env.example` is committed.

## CI/CD (GitHub Actions)
1. On pull request: lint, spec lint, backend build + tests (Testcontainers), frontend typecheck + tests. E2E (Playwright) on PR or nightly.
2. On push to `master`: run the same checks, build the backend and web images, push them to GHCR tagged with the commit SHA and `latest`.
3. Deploy job: SSH to the VPS (a dedicated deploy user, key stored in GitHub secrets) and run `docker compose -f deploy/docker-compose.prod.yml pull && … up -d`, then wait for `/readyz` and fail the job if it doesn't come up. Rollback = redeploy a previous SHA tag.
4. The server pulls images from GHCR using a read-only token (or the images are public).

## Scheduling
- The daily set is created by an in-process Spring `@Scheduled(cron = "0 0 0 * * *", zone = "UTC")` job, guarded by the Postgres advisory lock and the `daily_sets` primary key (safe even if two instances ever ran).
- Lazy generation on first request remains the fallback (e.g. the app was down at midnight).
- `POST /api/internal/generate` (with `X-Internal-Token`) stays available for manual runs; Caddy blocks `/api/internal/*` from the public internet (`respond 404`), so it's invoked from the server itself (`docker compose exec`) or via an SSH tunnel.
- `generation.preGenerateDays=0` initially.

## Backups and data
- Nightly `pg_dump` (cron on the host or a `postgres-backup` sidecar container) compressed and uploaded to cheap object storage (e.g. Backblaze B2 / any S3-compatible), 14 daily + 8 weekly retention. Restore procedure documented in a `deploy/README.md` and tested once.
- Provider VPS snapshots/backups are an optional additional layer (usually +20% cost).
- The data set is small (one row set per day), so storage is negligible; the main risk is losing the VPS disk, hence off-box backups.

## Host hardening (one-time)
- Create a non-root deploy user; disable SSH password login and root login; `ufw` allow 22/80/443 only; `unattended-upgrades` for security patches; fail2ban optional.
- Docker log rotation; `docker system prune` weekly via cron for old images.

## Environment variables
| Var | Purpose |
|---|---|
| `DOMAIN` | Used by Caddy |
| `POSTGRES_PASSWORD` (and user/db) | Postgres container |
| `SPRING_R2DBC_URL`, `SPRING_R2DBC_USERNAME`, `SPRING_R2DBC_PASSWORD` | App DB connection |
| `SPRING_FLYWAY_URL`, `…USER`, `…PASSWORD` | JDBC connection for migrations |
| `TMDB_READ_TOKEN` (or `TMDB_API_KEY`) | TMDB credentials |
| `INTERNAL_TOKEN` | Shared secret for `/internal/generate` |
| `GAME_*`, `TMDB_MIN_VOTE_COUNT`, … | Overrides for config in `../rules/06-configuration.md` |

No CORS configuration is needed (single origin through Caddy).

## Resource notes
- JVM: `-XX:MaxRAMPercentage=60`, container memory limit ~768 MB for the backend, ~384 MB for Postgres (`shared_buffers` ~128 MB). Startup time (~10-20 s) doesn't matter on an always-on host.
- Observability: Actuator health, `docker compose logs`, and an external uptime check (free tier of any uptime monitor) on `/healthz`.

## Moving off the VPS later
The images, env vars and Postgres URL are all that the application needs, so migrating to a managed platform (Render, Fly.io, Railway) means swapping Caddy for the platform's static hosting + `/api` rewrite and pointing at a managed Postgres. No application changes.

## Security basics
- HTTPS everywhere via Caddy (HSTS enabled); no secrets in images or the repo; `INTERNAL_TOKEN` rotated by editing `.env` and redeploying.
- Dependency updates via Dependabot/Renovate (Gradle, pnpm, Docker base images, GitHub Actions).
