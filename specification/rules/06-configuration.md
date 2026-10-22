# Configuration

Game parameters live in a config file (e.g. Spring `application.yml`, section `game`), not in code. Secrets come from the environment.

```yaml
game:
  roundsPerGame: 7        # movies per daily set
  attemptsPerRound: 3     # guesses per movie

tmdb:
  apiKey: ${TMDB_API_KEY}
  minVoteCount: 5000
  minRuntime: 60
  excludedGenres: [99]    # Documentary
  originalLanguage: null  # e.g. "en"
  maxPoolPages: 100
  includeAdult: false

generation:
  cron: "0 0 0 * * *"     # UTC
  maxAttempts: 100
  preGenerateDays: 0
  noRepeatDays: 180       # a movie can't reappear within this many days

redaction:
  maxMaskedRatio: 0.35
  mask: "▇▇▇"
```

- Changing `roundsPerGame`/`attemptsPerRound` applies to games generated afterwards. A game stores the values it was generated with, so an existing game never changes its rules.
- The frontend reads these values from the API and never hardcodes them.
