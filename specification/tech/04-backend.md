# Backend

## Stack
Kotlin, Spring Boot 4.x, `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, Jackson Kotlin module, Actuator, Flyway, `kotlinx-coroutines-reactor`. Gradle Kotlin DSL; JDK 21+.

## Packages (feature-based, root `xyz.engsmyre.moviedescriptiongame`)
```
game/        GameController, PlaythroughService, PlaythroughRepository, domain (Playthrough, Round)
challenge/   GameGenerator, GameRepository, GenerationController (internal), MovieSelector
clue/        Clue model (flat, mirrors the API), ClueBuilder, Redactor
player/      PlayerController, PlayerService, TokenService, auth filter
stats/       StatsController, StatsService
tmdb/        TmdbClient (WebClient), TMDB DTOs, SearchController
config/      @ConfigurationProperties classes, Clock bean, Jackson, security/web config
common/      Problem+json error handling, utilities
```
Rules: controllers implement generated OpenAPI interfaces and only map DTOs ⇄ domain; business logic lives in services; repositories are only called by services of the same feature. Domain types are separate from generated API models; persistence uses `DatabaseClient` with plain SQL and small repository classes (no ORM entities), so what runs against the database is always visible.

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
- There is no reveal call: clues unlock by themselves. `GameViewMapper` computes which clues of a round are unlocked from the number of attempts (`order <= guesses + skips + 1`, or all once the round ended), so nothing about unlocking is stored.
- `skip(player)` takes the same row lock as `guess`, increments `playthrough_round.skips` and shares the round-ending logic (`settle`) with `guess`, so skipping the last attempt fails the round. Attempt numbers of guesses count skips too.
- Client view mapping happens in one place (`GameViewMapper`), which is the only code allowed to expose clue content or answers, applying the unlock rules from the functional spec.

## Challenge generation
- `GameGenerator.ensure(date)`: if a game exists for the date (via `daily_game`), return it. Otherwise it reads the movie ids used within `generation.noRepeatDays`, asks `MovieSelector` for `roundsPerGame` movies, then in **one transaction** takes the date's Postgres advisory lock (`pg_advisory_xact_lock`), re-checks for an existing game, and inserts `movie` (insert-or-keep), `game`, `game_movie` and `daily_game`. A partial game is never visible.
- **Selection happens outside the transaction**, because it makes many slow TMDB calls and must not hold a database connection. The lock therefore does not prevent two instances from both selecting; it guarantees that only the first insert wins and the second returns the winner's game. Wasted TMDB calls in that rare race are accepted.
- Existing `movie` rows are never overwritten (`ON CONFLICT DO NOTHING`): a movie that returns after the no-repeat window keeps the clues it was first generated with, so past playthroughs stay reproducible.
- If `maxAttempts` candidates are examined without finding enough usable movies, generation fails with `GENERATION_FAILED` (503) and the log lists why candidates were rejected.
- `MovieSelector` implements the discover → validate → build-clues pipeline from `../rules/03-challenge-generation.md` using an injectable `kotlin.random.Random` (seeded in tests). It fetches the first page to learn `total_pages`, then samples random pages (caching them for the run).
- `ClueBuilder` produces the stored clues or a rejection reason; `Redactor` is a pure function (title/names → text → masked text + counts) with its own tests.
- `GenerationScheduler` runs `ensure` for today (and `preGenerateDays` ahead) at `generation.cron` in UTC; disabled with `generation.scheduler-enabled=false` (tests).

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
