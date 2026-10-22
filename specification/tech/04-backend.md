# Backend

## Stack
Kotlin, Spring Boot 4.x, `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, Jackson Kotlin module, Actuator, Flyway, `kotlinx-coroutines-reactor`. Gradle Kotlin DSL; JDK 21+.

## Packages (feature-based, root `xyz.engsmyre.moviedescriptiongame`)
```
game/        GameController, PlaythroughService, PlaythroughRepository, domain (Playthrough, Round, RevealPolicy)
challenge/   GameGenerator, GameRepository, GenerationController (internal), MovieSelector
clue/        Clue model (sealed), ClueBuilder, Redactor
player/      PlayerController, PlayerService, TokenService, auth filter
stats/       StatsController, StatsService
tmdb/        TmdbClient (WebClient), TMDB DTOs, SearchController
config/      @ConfigurationProperties classes, Clock bean, Jackson, security/web config
common/      Problem+json error handling, utilities
```
Rules: controllers implement generated OpenAPI interfaces and only map DTOs ⇄ domain; business logic lives in services; repositories are only called by services of the same feature. Domain types are separate from generated API models and from persistence entities, with mappers between them.

## Coroutines
- Controllers and services are `suspend`; collections are `Flow` only when streaming makes sense.
- No blocking calls on event-loop threads (no JDBC in request path; Flyway runs at startup only). Blocking work, if ever needed, uses `Dispatchers.IO`.
- Outbound TMDB calls use `WebClient` with timeouts (connect 2s, response 5s), bounded concurrency, and retry with backoff for 429/5xx (respect `Retry-After`).

## Authentication
- `POST /players` generates 32 random bytes (SecureRandom), returns it URL-safe base64 as the token, and stores `SHA-256(token)`.
- A `WebFilter` resolves `Authorization: Bearer` → player (lookup by hash), puts the player id into the coroutine/Reactor context, and updates `last_seen_at` (throttled, at most once per hour per player).
- Public endpoints that skip the filter: `POST /players`, health, and `/internal/**` (own token check).
- Plain Spring Security is not required; add it only if more auth needs appear.

## Game flow implementation
- `PlaythroughService.startToday(player)`: ensures today's game exists (via `daily_game`) (lazy generation), then inserts the `playthrough` and one `playthrough_round` row per movie in the game, with round 0 `ACTIVE`; relies on `UNIQUE(player_id, date)` for idempotence (on conflict, return the existing game).
- `guess(player, tmdbId)`: look up the guessed movie on TMDB (validates the id, gives title/year) → single transaction → lock the active round → validate → insert guess → compute correctness against the set's movie id → update round/game status → advance `current_round` and activate the next round when ended → build the response.
- `reveal(player, clueId)`: validates the clue exists for the active round; `INSERT … ON CONFLICT DO NOTHING`; returns the clue content.
- **Reveal policy** is an interface (`RevealPolicy`) with the default `FreeRevealPolicy`; it is called by `reveal` and `guess` and can reject or auto-reveal. Keep this seam.
- Client view mapping happens in one place (`GameViewMapper`), which is the only code allowed to expose clue content or answers, applying the redaction-of-unearned rules from the functional spec.

## Challenge generation
- `GameGenerator.ensure(date)`: under the date's advisory lock, if a `daily_game` row exists for the date return its game; otherwise generate, upsert the `movie` rows and insert the `game`, `game_movie` and `daily_game` rows in one transaction (so a partial set is never visible). A concurrent generator hitting the primary key / unique constraint rolls back and re-reads. Use a Postgres advisory lock (`pg_advisory_xact_lock(hash(date))`) around generation to avoid wasted TMDB calls across instances.
- `MovieSelector` implements the discover → validate → build-clues pipeline from `../rules/03-challenge-generation.md` using an injectable `Random` (seedable for tests).
- `Redactor` is a pure function (title/people → text → masked text + ratio); heavily unit-tested.

## TMDB client
- Endpoints used: `/discover/movie`, `/movie/{id}?append_to_response=credits`, `/search/movie`. Auth via the v4 read token header (`Authorization: Bearer`) or v3 `api_key` param, from env `TMDB_API_KEY`/`TMDB_READ_TOKEN`.
- Responses are mapped to small DTOs ignoring unknown fields. In-memory cache (Caffeine, short TTL) for search results to reduce load.
- Base URL configurable (for WireMock in tests and e2e).

## Configuration
`@ConfigurationProperties` prefixes `game`, `tmdb`, `generation`, `redaction` as in `../rules/06-configuration.md`, validated at startup (fail fast). Secrets only via env: `TMDB_*`, `INTERNAL_TOKEN`, `SPRING_R2DBC_URL/USERNAME/PASSWORD`, `SPRING_FLYWAY_URL/…`.

## Observability
- Structured JSON logs in non-dev profiles; request id propagated through MDC/Reactor context.
- Never log tokens. Log generation runs (date, attempts used, rejected candidates and why).
- Micrometer metrics via Actuator: TMDB call latency/errors, generation duration, games started/completed.
