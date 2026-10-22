package xyz.engsmyre.moviedescriptiongame.tmdb

import org.springframework.http.HttpStatus
import xyz.engsmyre.moviedescriptiongame.common.ApiException

data class TmdbDiscoverPage(val page: Int, val totalPages: Int, val results: List<TmdbMovieSummary>)

data class TmdbMovieSummary(val id: Int, val title: String, val releaseYear: Int?)

data class TmdbCastMember(val name: String, val character: String?, val order: Int)

data class TmdbCrewMember(val name: String, val job: String)

data class TmdbMovieDetails(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val overview: String?,
    val tagline: String?,
    val releaseYear: Int?,
    val runtimeMinutes: Int?,
    val posterPath: String?,
    val genres: List<String>,
    val cast: List<TmdbCastMember>,
    val crew: List<TmdbCrewMember>,
)

class TmdbUnavailableException(detail: String, cause: Throwable? = null) :
    ApiException(HttpStatus.SERVICE_UNAVAILABLE, "TMDB_UNAVAILABLE", "Movie database unavailable", detail) {
    init {
        if (cause != null) initCause(cause)
    }
}
