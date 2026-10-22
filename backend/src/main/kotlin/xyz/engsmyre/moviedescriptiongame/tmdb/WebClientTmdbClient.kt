package xyz.engsmyre.moviedescriptiongame.tmdb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.util.retry.Retry
import xyz.engsmyre.moviedescriptiongame.config.TmdbProperties
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component
class WebClientTmdbClient(
    builder: WebClient.Builder,
    private val props: TmdbProperties,
) : TmdbClient {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client: WebClient = builder
        .baseUrl(props.baseUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${props.readToken}")
        .defaultHeader(HttpHeaders.ACCEPT, "application/json")
        .build()

    override suspend fun discover(page: Int): TmdbDiscoverPage {
        val dto = get<DiscoverDto>("discover") { uri ->
            uri.path("/discover/movie")
                .queryParam("sort_by", "vote_count.desc")
                .queryParam("vote_count.gte", props.minVoteCount)
                .queryParam("with_runtime.gte", props.minRuntime)
                .queryParam("include_adult", props.includeAdult)
                .queryParam("language", "en-US")
                .queryParam("page", page)
                .apply {
                    // '|' means "any of": exclude a movie if it has any of these genres
                    if (props.excludedGenres.isNotEmpty()) {
                        queryParam("without_genres", props.excludedGenres.joinToString("|"))
                    }
                    props.originalLanguage?.let { queryParam("with_original_language", it) }
                }
        } ?: throw TmdbUnavailableException("Empty discover response")
        return TmdbDiscoverPage(dto.page, dto.totalPages, dto.results.mapNotNull { it.toSummary() })
    }

    override suspend fun details(id: Int): TmdbMovieDetails? {
        val dto = get<DetailsDto>("details") { uri ->
            uri.path("/movie/$id").queryParam("append_to_response", "credits").queryParam("language", "en-US")
        } ?: return null
        return TmdbMovieDetails(
            id = dto.id,
            title = dto.title.orEmpty(),
            originalTitle = dto.originalTitle ?: dto.title.orEmpty(),
            overview = dto.overview,
            tagline = dto.tagline,
            releaseYear = yearOf(dto.releaseDate),
            runtimeMinutes = dto.runtime,
            posterPath = dto.posterPath,
            genres = dto.genres.mapNotNull { it.name },
            cast = dto.credits?.cast.orEmpty().map { TmdbCastMember(it.name, it.character, it.order) },
            crew = dto.credits?.crew.orEmpty().map { TmdbCrewMember(it.name, it.job.orEmpty()) },
        )
    }

    override suspend fun find(id: Int): TmdbMovieSummary? =
        get<ListItemDto>("find") { uri -> uri.path("/movie/$id").queryParam("language", "en-US") }?.toSummary()

    override suspend fun search(query: String): List<TmdbMovieSummary> {
        val dto = get<DiscoverDto>("search") { uri ->
            uri.path("/search/movie")
                .queryParam("query", query)
                .queryParam("include_adult", false)
                .queryParam("language", "en-US")
                .queryParam("page", 1)
        } ?: return emptyList()
        return dto.results.mapNotNull { it.toSummary() }
    }

    /** GET with timeout and retry on transient failures. Returns null on 404. */
    private suspend inline fun <reified T : Any> get(
        what: String,
        crossinline uri: (org.springframework.web.util.UriBuilder) -> Any?,
    ): T? {
        try {
            return client.get()
                .uri { b -> uri(b); b.build() }
                .retrieve()
                .bodyToMono<T>()
                .timeout(Duration.ofSeconds(props.requestTimeoutSeconds))
                .retryWhen(
                    Retry.backoff(props.maxRetries, Duration.ofMillis(props.retryBackoffMillis))
                        .filter(::isTransient)
                        .onRetryExhaustedThrow { _, signal -> signal.failure() }
                )
                .awaitSingleOrNull()
        } catch (e: CancellationException) {
            throw e
        } catch (e: WebClientResponseException.NotFound) {
            return null
        } catch (e: Exception) {
            log.warn("TMDB {} failed: {}", what, e.message)
            throw TmdbUnavailableException("TMDB request '$what' failed: ${e.message}", e)
        }
    }

    private fun isTransient(e: Throwable): Boolean = when (e) {
        is WebClientResponseException ->
            e.statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value() || e.statusCode.is5xxServerError
        is WebClientRequestException, is TimeoutException -> true
        else -> false
    }

    private fun yearOf(date: String?): Int? = date?.takeIf { it.length >= 4 }?.substring(0, 4)?.toIntOrNull()

    private fun ListItemDto.toSummary(): TmdbMovieSummary? =
        title?.takeIf { it.isNotBlank() }?.let { TmdbMovieSummary(id, it, yearOf(releaseDate)) }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DiscoverDto(
        val page: Int = 1,
        @JsonProperty("total_pages") val totalPages: Int = 1,
        val results: List<ListItemDto> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ListItemDto(
        val id: Int = 0,
        val title: String? = null,
        @JsonProperty("release_date") val releaseDate: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DetailsDto(
        val id: Int = 0,
        val title: String? = null,
        @JsonProperty("original_title") val originalTitle: String? = null,
        val overview: String? = null,
        val tagline: String? = null,
        @JsonProperty("release_date") val releaseDate: String? = null,
        val runtime: Int? = null,
        @JsonProperty("poster_path") val posterPath: String? = null,
        val genres: List<GenreDto> = emptyList(),
        val credits: CreditsDto? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GenreDto(val name: String? = null)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CreditsDto(val cast: List<CastDto> = emptyList(), val crew: List<CrewDto> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CastDto(val name: String = "", val character: String? = null, val order: Int = Int.MAX_VALUE)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CrewDto(val name: String = "", val job: String? = null)
}
