package xyz.engsmyre.moviedescriptiongame.challenge

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import xyz.engsmyre.moviedescriptiongame.clue.ClueBuilder
import xyz.engsmyre.moviedescriptiongame.config.GenerationProperties
import xyz.engsmyre.moviedescriptiongame.config.RedactionProperties
import xyz.engsmyre.moviedescriptiongame.config.TmdbProperties
import xyz.engsmyre.moviedescriptiongame.support.Fixtures
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbClient
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbDiscoverPage
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbMovieDetails
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbMovieSummary
import kotlin.random.Random

class MovieSelectorTest {

    /** [pages] pages of [perPage] movies each; [broken] ids have no tagline and are unusable. */
    private class FakeTmdb(val pages: Int, val perPage: Int, val broken: Set<Int> = emptySet()) : TmdbClient {
        val detailsCalls = mutableListOf<Int>()
        override suspend fun discover(page: Int) = TmdbDiscoverPage(
            page, pages,
            (1..perPage).map { i -> (page - 1) * perPage + i }.map { TmdbMovieSummary(it, "Movie $it Title", 2000) },
        )
        override suspend fun details(id: Int): TmdbMovieDetails? {
            detailsCalls += id
            return Fixtures.details(id, tagline = if (id in broken) null else "A tagline")
        }
        override suspend fun find(id: Int) = null
        override suspend fun search(query: String) = emptyList<TmdbMovieSummary>()
    }

    private fun selector(tmdb: TmdbClient, maxAttempts: Int = 100, maxPoolPages: Int = 100) = MovieSelector(
        tmdb, ClueBuilder(RedactionProperties()), TmdbProperties(maxPoolPages = maxPoolPages),
        GenerationProperties(maxAttempts = maxAttempts),
    )

    @Test
    fun `selects the requested number of distinct movies`() = runTest {
        val movies = selector(FakeTmdb(pages = 3, perPage = 20)).select(7, emptySet(), Random(1))
        assertEquals(7, movies.size)
        assertEquals(7, movies.map { it.id }.toSet().size)
        assertTrue(movies.all { it.clues.size == 4 })
    }

    @Test
    fun `never picks excluded movies`() = runTest {
        val excluded = (1..50).toSet()
        val movies = selector(FakeTmdb(pages = 3, perPage = 20)).select(7, excluded, Random(2))
        assertTrue(movies.none { it.id in excluded })
    }

    @Test
    fun `skips movies that cannot be turned into clues`() = runTest {
        val broken = (1..30).toSet()
        val tmdb = FakeTmdb(pages = 3, perPage = 20, broken = broken)
        val movies = selector(tmdb).select(7, emptySet(), Random(3))
        assertEquals(7, movies.size)
        assertFalse(movies.any { it.id in broken })
    }

    @Test
    fun `is deterministic for a given seed`() = runTest {
        val a = selector(FakeTmdb(3, 20)).select(7, emptySet(), Random(42)).map { it.id }
        val b = selector(FakeTmdb(3, 20)).select(7, emptySet(), Random(42)).map { it.id }
        assertEquals(a, b)
    }

    @Test
    fun `only samples from the configured number of pages`() = runTest {
        val tmdb = FakeTmdb(pages = 50, perPage = 20)
        val movies = selector(tmdb, maxPoolPages = 2).select(7, emptySet(), Random(5))
        assertTrue(movies.all { it.id <= 40 }, movies.map { it.id }.toString())
    }

    @Test
    fun `gives up when the pool is exhausted`() {
        val tmdb = FakeTmdb(pages = 1, perPage = 5)
        val e = assertThrows(NotEnoughMoviesException::class.java) {
            kotlinx.coroutines.test.runTest { selector(tmdb, maxAttempts = 20).select(7, emptySet(), Random(1)) }
        }
        assertTrue(e.message!!.contains("Only found 5 of 7"), e.message)
    }
}
