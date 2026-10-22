package xyz.engsmyre.moviedescriptiongame.tmdb

/** The parts of TMDB the game needs. Implementations throw [TmdbUnavailableException] on failures. */
interface TmdbClient {
    /** One page of the candidate pool, sorted by vote count (most voted first). */
    suspend fun discover(page: Int): TmdbDiscoverPage

    /** Full details with credits, or null if TMDB does not know the movie. */
    suspend fun details(id: Int): TmdbMovieDetails?

    /** Basic info about a movie, or null if TMDB does not know it. */
    suspend fun find(id: Int): TmdbMovieSummary?

    suspend fun search(query: String): List<TmdbMovieSummary>
}
