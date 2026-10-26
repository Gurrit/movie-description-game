package xyz.engsmyre.moviedescriptiongame.tmdb

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.web.reactive.function.client.WebClient
import xyz.engsmyre.moviedescriptiongame.config.TmdbProperties

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebClientTmdbClientTest {
    private lateinit var server: WireMockServer
    private lateinit var client: WebClientTmdbClient

    @BeforeAll fun start() {
        server = WireMockServer(options().dynamicPort()).also { it.start() }
        client = WebClientTmdbClient(
            WebClient.builder(),
            TmdbProperties(
                baseUrl = "http://localhost:${server.port()}/3", readToken = "secret-token",
                minVoteCount = 5000, minRuntime = 60, excludedGenres = listOf(99, 16), maxRetries = 2, retryBackoffMillis = 10,
            ),
        )
    }

    @AfterAll fun stop() = server.stop()

    @BeforeEach fun reset() = server.resetAll()

    private fun json(body: String) = aResponse().withHeader("Content-Type", "application/json").withBody(body)

    @Test
    fun `discover sends the pool filters and parses the page`() = runTest {
        server.stubFor(
            get(urlPathEqualTo("/3/discover/movie")).willReturn(
                json("""{"page":3,"total_pages":42,"results":[
                    {"id":11,"title":"Star Wars","release_date":"1977-05-25","popularity":1.0},
                    {"id":12,"title":"","release_date":""}]}""")
            )
        )
        val page = client.discover(3)

        assertEquals(3, page.page)
        assertEquals(42, page.totalPages)
        assertEquals(listOf(TmdbMovieSummary(11, "Star Wars", 1977)), page.results)
        server.verify(
            getRequestedFor(urlPathEqualTo("/3/discover/movie"))
                .withHeader("Authorization", equalTo("Bearer secret-token"))
                .withQueryParam("sort_by", equalTo("vote_count.desc"))
                .withQueryParam("vote_count.gte", equalTo("5000"))
                .withQueryParam("with_runtime.gte", equalTo("60"))
                .withQueryParam("include_adult", equalTo("false"))
                .withQueryParam("without_genres", equalTo("99|16"))
                .withQueryParam("page", equalTo("3"))
        )
    }

    @Test
    fun `details parses credits and metadata`() = runTest {
        server.stubFor(
            get(urlPathEqualTo("/3/movie/11")).willReturn(
                json("""{"id":11,"title":"Star Wars","original_title":"Star Wars","overview":"A farm boy.","tagline":"A long time ago",
                    "release_date":"1977-05-25","runtime":121,"poster_path":"/p.jpg","genres":[{"id":1,"name":"Sci-Fi"}],
                    "production_companies":[{"id":1,"name":"Lucasfilm"},{"id":2,"name":"Twentieth Century Fox"}],
                    "credits":{"cast":[{"name":"Mark Hamill","character":"Luke","order":0}],
                               "crew":[{"name":"George Lucas","job":"Director"}]}}""")
            )
        )
        val d = client.details(11)!!

        assertEquals("Star Wars", d.title)
        assertEquals(1977, d.releaseYear)
        assertEquals(121, d.runtimeMinutes)
        assertEquals("/p.jpg", d.posterPath)
        assertEquals(listOf("Sci-Fi"), d.genres)
        assertEquals(listOf("Lucasfilm", "Twentieth Century Fox"), d.studios)
        assertEquals(listOf(TmdbCastMember("Mark Hamill", "Luke", 0)), d.cast)
        assertEquals(listOf(TmdbCrewMember("George Lucas", "Director")), d.crew)
        server.verify(getRequestedFor(urlPathEqualTo("/3/movie/11")).withQueryParam("append_to_response", equalTo("credits")))
    }

    @Test
    fun `unknown movies are null`() = runTest {
        server.stubFor(get(urlPathEqualTo("/3/movie/404")).willReturn(aResponse().withStatus(404).withBody("{}")))
        assertNull(client.details(404))
        assertNull(client.find(404))
    }

    @Test
    fun `search returns summaries`() = runTest {
        server.stubFor(
            get(urlPathEqualTo("/3/search/movie")).willReturn(
                json("""{"results":[{"id":1,"title":"Alien","release_date":"1979-05-25"},{"id":2,"title":"Aliens","release_date":null}]}""")
            )
        )
        assertEquals(
            listOf(TmdbMovieSummary(1, "Alien", 1979), TmdbMovieSummary(2, "Aliens", null)),
            client.search("alien"),
        )
        server.verify(getRequestedFor(urlPathEqualTo("/3/search/movie")).withQueryParam("query", equalTo("alien")))
    }

    @Test
    fun `retries transient failures`() = runTest {
        server.stubFor(
            get(urlPathEqualTo("/3/movie/5")).inScenario("flaky").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503)).willSetStateTo("recovered")
        )
        server.stubFor(
            get(urlPathEqualTo("/3/movie/5")).inScenario("flaky").whenScenarioStateIs("recovered")
                .willReturn(json("""{"id":5,"title":"Up","release_date":"2009-05-29"}"""))
        )
        assertEquals(TmdbMovieSummary(5, "Up", 2009), client.find(5))
    }

    @Test
    fun `gives up with TmdbUnavailableException after the retries`() {
        server.stubFor(get(urlPathEqualTo("/3/movie/6")).willReturn(aResponse().withStatus(500)))
        val e = assertThrows(TmdbUnavailableException::class.java) { kotlinx.coroutines.test.runTest { client.find(6) } }
        assertEquals("TMDB_UNAVAILABLE", e.code)
        assertTrue(server.allServeEvents.size == 3, "1 call + 2 retries, was ${server.allServeEvents.size}")
    }

    @Test
    fun `does not retry client errors`() {
        server.stubFor(get(urlPathEqualTo("/3/movie/7")).willReturn(aResponse().withStatus(401)))
        assertThrows(TmdbUnavailableException::class.java) { kotlinx.coroutines.test.runTest { client.find(7) } }
        assertEquals(1, server.allServeEvents.size)
    }
}
