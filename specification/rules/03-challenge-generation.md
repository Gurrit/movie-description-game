# Challenge Generation

## Schedule
- A scheduled job runs at **00:00 UTC** daily and creates the daily set for that date.
- **Lazy fallback**: if a request arrives and today's set does not exist (job missed, fresh deploy), it is generated on demand. Generation is idempotent and guarded by a unique index on `date`, so concurrent attempts yield one set.
- The job also may pre-generate tomorrow's set (optional, `generation.preGenerateDays`, default 0).
- Games are **never deleted**. The database keeps all history; only today's game is playable.

## Selecting movies from TMDB
Goal: well-known, mainstream movies with complete data, with enough variety that the same films don't dominate.

### Candidate pool query
Use `GET /discover/movie` with:

| Param | Value (config) | Reason |
|---|---|---|
| `vote_count.gte` | `tmdb.minVoteCount` (default 5000, tune) | Known by many players, with reliable data |
| `include_adult` | `false` | |
| `without_genres` | `99` (Documentary) (config list) | Exclude documentaries |
| `with_runtime.gte` | `tmdb.minRuntime` (default 60) | Exclude shorts |
| `with_original_language` | `tmdb.originalLanguage` (default unset = any) | Optional: e.g. `en` |
| `sort_by` | `vote_count.desc` | Stable ordering, so paging is deterministic |
| `page` | random in `[1, poolPages]` | Variety |

- `vote_count.gte` alone defines the pool; no `vote_average` filter (a famous bad movie is fine).
- `poolPages` = `min(tmdb.maxPoolPages, total_pages)` (TMDB caps `page` at 500; 20 results per page). Default `maxPoolPages` = 100 (≈ 2000 candidates), is configurable. The pool must comfortably exceed `noRepeatDays × roundsPerGame` (default 180 × 7 = 1260), otherwise the generator runs out of fresh candidates; increase `maxPoolPages` or lower `noRepeatDays` if generation fails with exhausted candidates.
- Sorting by `vote_count.desc` and sampling from the first N pages means the pool is "the N most-voted eligible films", which is a proxy for recognizability. If the pool is too small, lower `minVoteCount`. If too obscure, lower `maxPoolPages`.

### Per-candidate validation
1. Pick a random result from a random page.
2. Reject if the movie appeared in a game within the last `generation.noRepeatDays` days (default 180), or is already in today's game. Older appearances are allowed: a movie may return after the window.
3. Fetch `/movie/{id}?append_to_response=credits` (single call).
4. Reject if required data is missing ([02-clues.md](02-clues.md#required-data)) or redaction ratio check fails.
5. Build the clues and store the movie snapshot (id, title, original title, year, poster path, clues).
6. Repeat until `roundsPerGame` movies are collected. Give up after `generation.maxAttempts` (default 100) TMDB candidates and fail loudly (alert/log); the lazy path then returns 503 to clients.

### Resilience
- TMDB API key is a secret from the environment (`TMDB_API_KEY`). Retry transient errors (429/5xx) with exponential backoff.
- Persist the **snapshot** of every needed TMDB field so play does not depend on TMDB. Only the autocomplete needs TMDB at play time.
- Poster images are referenced by TMDB path (`poster_path`) and the client builds the URL from the TMDB image base. No images are stored.

## Autocomplete
- `GET /movies/random` serves the Random button: a random movie from a random page of the same discover pool (pages cached for an hour), also behind a player token and the search rate limit.
- `GET /movies/search?q=` is proxied through the backend (`/search/movie`), which keeps the API key off the client. It returns `{tmdbId, title, year}` for up to 10 results, debounce on the client (≥250 ms), min 2 chars. It requires a valid player token and is rate limited.
- Search results are not restricted to the candidate pool: any movie can be guessed.
