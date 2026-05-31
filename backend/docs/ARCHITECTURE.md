# Movie Description Game - Backend Architecture

## Overview

A daily movie guessing game where players guess movies based on progressive clues (year, director, actors, etc.). Each day, 5 random movies are selected and stored. Players request clues, lose points per clue, and submit guesses to earn scores.

## Architecture Diagram

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────────────┐
│   TMDB API       │◄────┤ PopularMoviesTmdb│     │         AdminService        │
│   (External)      │     │        Client     │     │  - generateDailyChallenge()  │
└─────────────────┘     └──────────────────┘     │  - getChallenge(date)         │
                                                  └──────────────────┬────────────────┘
                                                                       │
┌─────────────────────────────┐       ┌──────────────────▼──────────────────┐
│      MongoDB                  │       │        ChallengeService               │
│  ┌─────────────────────────┐  │       │  - getOrCreateToday()                │
│  │  challenge               │  │       │  - getClue(challenge, clueType)     │
│  │  - day: String           │  │       │  - validateGuess()                   │
│  │  - movies: [MovieSub]    │  │       └──────────────────┬──────────────────┘
│  └─────────────────────────┘  │                                │
│  ┌─────────────────────────┐  │               ┌────────────▼────────────┐
│  │  score                   │  │               │       ClueService        │
│  │  - sessionId: String     │  │               │  - getClueAndLowerScore() │
│  │  - cluesUsed: [String]    │  │               │  - calculateScore()       │
│  │  - currentScore: Int     │  │               └──────────────────────────┘
│  └─────────────────────────┘  │                        │
└─────────────────────────────┘                        │
                         ↑                              │
                         │                              ▼
┌─────────────────────────────┐       ┌─────────────────────────────┐
│      REST API (Controllers)   │◄──────┤        Frontend               │
│  ┌─────────────────────────┐  │       │  (React / Mobile)            │
│  │ POST /admin/refresh      │  │       │                              │
│  │ GET  /challenge/today    │  │       └─────────────────────────────┘
│  │ POST /clue/{type}        │  │
│  │ POST /guess              │  │
│  └─────────────────────────┘  │
└─────────────────────────────┘
```

## Component Responsibilities

### 1. TMDB Adapter (`tmdb` package)
- **Purpose**: Fetch movie data from The Movie Database API
- **Key Classes**:
  - `PopularMoviesTmdbClient` - Fetches popular movies with vote count filtering
  - `TmdbMovieResponse` - DTO for API response
  - `Movie` - Domain model for movie data
- **Config**: API key via `tmdb.api_key` environment variable

### 2. Core Domain (`core` package)

#### Services
- **AdminService**
  - `generateDailyChallenge()`: Fetches 5 random movies from TMDB, persists to MongoDB
  - `getChallenge(date: LocalDate)`: Retrieves challenge for specific date
  
- **ChallengeService**
  - `getOrCreateToday()`: Returns today's challenge, generates if not exists
  - Uses cron job or startup hook to ensure daily challenge exists

- **ClueService**
  - `getClue(sessionId: String, clueType: ClueType)`: Returns clue value, records usage
  - `calculateScore(sessionId: String, guess: String)`: Validates guess, calculates points
  - **Clue Types**: `YEAR`, `DIRECTOR`, `ACTOR_1`, `ACTOR_2`, `ACTOR_3`, `GENRE`, `PLOT_HINT`, `TITLE_HINT`

- **ScoreService**
  - Tracks points per session
  - Base score: 1000 points per movie
  - Clue penalties: Configurable per clue type (e.g., YEAR=-50, DIRECTOR=-100, ACTOR_1=-150)

#### Domain Models
```kotlin
// Movie.kt
data class Movie(
    val id: Int,           // TMDB ID
    val title: String,
    val originalTitle: String,
    val releaseYear: Int,
    val director: String,
    val actors: List<String>,  // Top 3
    val genres: List<String>,
    val plot: String,
    val posterUrl: String?
)

// Challenge.kt  
data class Challenge(
    val id: ObjectId,
    val date: LocalDate,
    val movies: List<Movie>
)

// ClueType.kt
enum class ClueType(
    val pointCost: Int
) {
    YEAR(50),
    DIRECTOR(100),
    ACTOR_1(150),
    ACTOR_2(200),
    ACTOR_3(250),
    GENRE(75),
    PLOT_HINT(300),
    TITLE_HINT(500)
}
```

### 3. Data Layer (`db` package)

#### Entities
- **ChallengeEntity** (collection: `challenge`)
  - Maps to `Challenge` domain model
  - Index on `day` field for fast lookup
  
- **ScoreEntity** (collection: `score`)
  - Tracks user session progress
  - Contains: `sessionId`, `challengeId`, `cluesUsed: List<ClueType>`, `guesses: List<Guess>`

#### Repositories
- **ChallengeRepository**
  - `insertChallenge(challenge: ChallengeEntity): ChallengeEntity`
  - `getChallenge(date: LocalDate): ChallengeEntity?`
  - `getOrCreateToday(): ChallengeEntity`

- **ScoreRepository**
  - `getOrCreateSession(sessionId: String, challengeId: ObjectId): ScoreEntity`
  - `addClue(sessionId: String, clueType: ClueType)`
  - `addGuess(sessionId: String, movieId: Int, correct: Boolean)`

### 4. API Layer (`controller` package)

#### Endpoints

| Method | Endpoint | Request | Response | Description |
|--------|----------|---------|----------|-------------|
| POST | `/admin/refresh` | - | `Challenge` | Force regenerate today's challenge |
| GET | `/challenge/today` | - | `Challenge` | Get today's movie set |
| GET | `/challenge/{date}` | - | `Challenge` | Get challenge for specific date |
| POST | `/clue/{type}` | `{ sessionId }` | `{ clue: String, remainingScore: Int }` | Get clue, deduct points |
| POST | `/guess` | `{ sessionId, movieId }` | `{ correct: Boolean, score: Int, movie: Movie }` | Submit guess |
| GET | `/session/{id}` | - | `ScoreEntity` | Get current session state |

#### Request/Response DTOs
```kotlin
// Requests
data class ClueRequest(val sessionId: String)
data class GuessRequest(val sessionId: String, val movieId: Int)

// Responses
data class ClueResponse(
    val clue: String,
    val clueType: ClueType,
    val remainingScore: Int,
    val cluesUsed: List<ClueType>
)

data class GuessResponse(
    val correct: Boolean,
    val score: Int,
    val movie: Movie?,
    val remainingMovies: List<Int>  // IDs of unguessed movies
)

data class SessionResponse(
    val score: Int,
    val cluesUsed: List<ClueType>,
    val guesses: List<GuessResult>
)
```

## Implementation Phases

### Phase 1: Core Challenge Generation ✅ (Mostly Done)
- [x] TMDB client integration
- [x] MongoDB repository setup
- [x] Challenge entity and DTOs
- [ ] Movie selection algorithm (filter by vote_count, random selection)
- [ ] Daily challenge cron job / startup hook

### Phase 2: Clue System
- [ ] Define `ClueType` enum with point costs
- [ ] Implement `ClueService.getClue()` with scoring
- [ ] Add clue tracking to `ScoreEntity`
- [ ] ClueController endpoint

### Phase 3: Guess & Scoring
- [ ] Implement `ClueService.validateGuess()`
- [ ] Scoring calculation logic
- [ ] GuessController endpoint
- [ ] Session state tracking

### Phase 4: Polish
- [ ] Input validation
- [ ] Error handling
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Unit tests for services

## Configuration

### Environment Variables
```bash
# TMDB
TMDB_API_KEY=your_api_key
TMDB_BASE_URL=https://api.themoviedb.org/3
TMDB_MINIMUM_VOTE_COUNT=5000

# MongoDB
SPRING_DATA_MONGODB_URI=mongodb://root:example@mongo:27017/moviegame?authSource=admin
SPRING_DATA_MONGODB_DATABASE=moviegame

# Server
SERVER_PORT=8080
```

## Data Flow Examples

### Daily Challenge Generation
```
1. AdminService.generateDailyChallenge()
2. PopularMoviesTmdbClient.getPopularMoviesFromPage(1)
3. Filter movies where vote_count >= 5000
4. Randomly select 5 movies
5. Map to MovieSubEntity
6. ChallengeRepository.insertChallenge() with today's date
```

### User Gameplay
```
1. GET /challenge/today -> Returns challenge ID and movie IDs
2. POST /clue/DIRECTOR with sessionId -> Returns "Christopher Nolan", score -= 100
3. POST /clue/ACTOR_1 -> Returns "Leonardo DiCaprio", score -= 150
4. POST /guess with movieId=123 -> Returns { correct: true, score: 750 }
```

## Open Questions

1. **Session Management**: Use JWT, cookie-based sessions, or simple UUID tokens?
2. **Daily Reset Time**: Midnight UTC or user's local time?
3. **Movie Pool**: Should we cache a pool of movies weekly to avoid API rate limits?
4. **Difficulty**: Should clue point costs be configurable per difficulty level?
5. **Historical Challenges**: Should users be able to play past days' challenges?

## File Structure Reference

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/xyz/engsmyre/moviedescriptiongame/
│   │   │   ├── config/           # Spring config
│   │   │   ├── controller/       # REST endpoints
│   │   │   ├── db/               # Entities & Repositories
│   │   │   │   ├── entity/
│   │   │   │   ├── repository/
│   │   │   │   └── mongo/
│   │   │   ├── dto/              # API DTOs
│   │   │   ├── exceptions/       # Custom exceptions
│   │   │   ├── service/          # Business logic
│   │   │   └── tmdb/             # TMDB integration
│   │   │       ├── domain/
│   │   │       ├── repository/
│   │   │       └── request_config/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── kotlin/xyz/engsmyre/moviedescriptiongame/
│           └── service/
└── docs/
    └── ARCHITECTURE.md          # This file
```
