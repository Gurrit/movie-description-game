package xyz.engsmyre.moviedescriptiongame.tmdb.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.Movie
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.PopularMovieRequest
import xyz.engsmyre.moviedescriptiongame.tmdb.exception.TmdbCommunicationFailedException
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.helpers.RequestHelper.doBlockingDiscoveryRequest

@Component
final class PopularMoviesTmdbClient @Autowired constructor(@Qualifier("TmdbDiscoveryClient") private val tmdbWebClient: WebClient): PopularMoviesClient {
    @Value("\${tmdb.api_key}")
    final private val apiKey: String? = null

    @Value("\${tmdb.minimum_vote_count}") // TODO should not be set by an environment variable, but by difficulty.
    final private val voteCount // Vote count is used to determine if a movie is considerred "known"
            = 5000

    @Throws(TmdbCommunicationFailedException::class)
    override fun getPopularMoviesFromPage(page: Int): List<Movie?> {
        val webClientParams = PopularMovieRequest(
            apiKey!!,
            page,
            voteCount
        ).createParamsMap()
        return doBlockingDiscoveryRequest(webClientParams, tmdbWebClient).movies!!
    }

    @get:Throws(TmdbCommunicationFailedException::class)
    override val popularMoviesPageCount: Int
        get() {
            val webClientParams = PopularMovieRequest(apiKey!!, voteCount).createParamsMap()
            return doBlockingDiscoveryRequest(webClientParams, tmdbWebClient).getnPages()
        }
}