# Configuration

Game parameters live in a config file (e.g. Spring `application.yml`, section `game`), not in code. Secrets come from the environment.

```yaml
game:
  roundsPerGame: 7        # movies per daily set
  attemptsPerRound: 7     # attempts per movie; one per clue, so the last attempt sees them all

tmdb:
  apiKey: ${TMDB_API_KEY}
  minVoteCount: 5000
  minRuntime: 60
  excludedGenres: [99]    # Documentary
  originalLanguage: null  # e.g. "en"
  maxPoolPages: 100
  includeAdult: false
  requestTimeoutSeconds: 5
  maxRetries: 3           # for 429/5xx/timeouts, exponential backoff
  retryBackoffMillis: 500

generation:
  cron: "0 0 0 * * *"     # UTC
  maxAttempts: 100
  preGenerateDays: 0
  noRepeatDays: 180       # a movie can't reappear within this many days
  schedulerEnabled: true  # false disables the in-process daily job (tests)
  internalToken: ${INTERNAL_TOKEN}  # blank disables /internal/generate

ratelimit:                # per IP and minute
  playersPerMinute: 20    # POST /players
  searchPerMinute: 90     # GET /movies/search

redaction:
  maxMaskedRatio: 0.35
  mask: "▇▇▇"
```

- Changing `roundsPerGame`/`attemptsPerRound` applies to games generated afterwards. A game stores the values it was generated with, so an existing game never changes its rules.
- The frontend reads these values from the API and never hardcodes them.
