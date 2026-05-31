package xyz.engsmyre.moviedescriptiongame.tmdb.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.TmdbCreditsResponse
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.TmdbMovieDetailsResponse
import xyz.engsmyre.moviedescriptiongame.tmdb.exception.TmdbCommunicationFailedException

@Component
class DefaultMovieDetailsClient @Autowired constructor(
    @Qualifier("TmdbMovieClient") private val tmdbMovieWebClient: WebClient,
    @Qualifier("TmdbCreditsClient") private val tmdbCreditsWebClient: WebClient
) : MovieDetailsClient {

    @Value("\${tmdb.api_key}")
    private val apiKey: String? = null

    override fun getMovieDetails(movieId: Int): TmdbMovieDetailsResponse {
        try {
            return tmdbMovieWebClient.get()
                .uri("/$movieId?api_key=$apiKey")
                .retrieve()
                .bodyToMono(TmdbMovieDetailsResponse::class.java)
                .block() ?: throw TmdbCommunicationFailedException("No response from TMDB")
        } catch (e: Exception) {
            throw TmdbCommunicationFailedException(e)
        }
    }

    override fun getCredits(movieId: Int): TmdbCreditsResponse {
        try {
            return tmdbCreditsWebClient.get()
                .uri("/$movieId/credits?api_key=$apiKey")
                .retrieve()
                .bodyToMono(TmdbCreditsResponse::class.java)
                .block() ?: throw TmdbCommunicationFailedException("No response from TMDB")
        } catch (e: Exception) {
            throw TmdbCommunicationFailedException(e)
        }
    }
}
