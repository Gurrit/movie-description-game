package xyz.engsmyre.moviedescriptiongame.challenge

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.clue.ClueBuilder
import xyz.engsmyre.moviedescriptiongame.clue.ClueResult
import xyz.engsmyre.moviedescriptiongame.config.GenerationProperties
import xyz.engsmyre.moviedescriptiongame.config.TmdbProperties
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbClient
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbDiscoverPage
import kotlin.random.Random

class NotEnoughMoviesException(message: String) : RuntimeException(message)

/**
 * Picks movies for a game: random candidates from the vote-count ranked pool, accepted only when
 * they are not excluded and have everything needed for all clues.
 */
@Component
class MovieSelector(
    private val tmdb: TmdbClient,
    private val clues: ClueBuilder,
    private val tmdbProps: TmdbProperties,
    private val genProps: GenerationProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun select(count: Int, excluded: Set<Int>, random: Random = Random): List<Movie> {
        val pages = HashMap<Int, TmdbDiscoverPage>()
        suspend fun page(n: Int) = pages.getOrPut(n) { tmdb.discover(n) }

        val poolPages = minOf(tmdbProps.maxPoolPages, page(1).totalPages).coerceAtLeast(1)
        val tried = HashSet<Int>(excluded)
        val picked = ArrayList<Movie>(count)
        var attempts = 0

        while (picked.size < count) {
            if (attempts >= genProps.maxAttempts) {
                throw NotEnoughMoviesException(
                    "Only found ${picked.size} of $count movies after $attempts candidates " +
                        "(pool of $poolPages pages, ${excluded.size} excluded)"
                )
            }
            val candidates = page(random.nextInt(1, poolPages + 1)).results.filter { it.id !in tried }
            if (candidates.isEmpty()) {
                attempts++ // a page with nothing new still counts, so a tiny pool cannot loop forever
                continue
            }
            val candidate = candidates.random(random)
            tried += candidate.id
            attempts++

            val details = tmdb.details(candidate.id)
            if (details == null) {
                log.info("Rejected {} ({}): unknown to TMDB", candidate.id, candidate.title)
                continue
            }
            when (val result = clues.build(details)) {
                is ClueResult.Rejected -> log.info("Rejected {} ({}): {}", candidate.id, candidate.title, result.reason)
                is ClueResult.Built -> picked += Movie(
                    id = details.id,
                    title = details.title,
                    originalTitle = details.originalTitle,
                    releaseYear = details.releaseYear!!,
                    posterPath = details.posterPath,
                    clues = result.clues,
                )
            }
        }
        log.info("Selected {} movies after {} candidates", picked.size, attempts)
        return picked
    }
}
