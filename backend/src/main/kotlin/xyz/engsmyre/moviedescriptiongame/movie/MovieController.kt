package xyz.engsmyre.moviedescriptiongame.movie

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.MovieApi
import xyz.engsmyre.moviedescriptiongame.api.model.MovieSuggestion
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbClient
import java.time.Duration

/** Autocomplete for guesses, proxied through the backend so the TMDB token stays server-side. */
@RestController
class MovieController(private val tmdb: TmdbClient) : MovieApi {

    private val cache = Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(Duration.ofMinutes(10)).build<String, List<MovieSuggestion>>()

    override fun searchMovies(q: String): ResponseEntity<Flow<MovieSuggestion>> {
        val query = q.trim()
        if (query.length < MIN_LENGTH || query.length > MAX_LENGTH) {
            throw ApiException(
                HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Invalid query",
                "q must be $MIN_LENGTH-$MAX_LENGTH characters",
            )
        }
        val key = query.lowercase()
        return ResponseEntity.ok(flow {
            val results = cache.getIfPresent(key) ?: tmdb.search(query).take(MAX_RESULTS)
                .map { MovieSuggestion(it.id, it.title, it.releaseYear) }
                .also { cache.put(key, it) }
            results.forEach { emit(it) }
        })
    }

    private companion object {
        const val MIN_LENGTH = 2
        const val MAX_LENGTH = 100
        const val MAX_RESULTS = 10
    }
}
