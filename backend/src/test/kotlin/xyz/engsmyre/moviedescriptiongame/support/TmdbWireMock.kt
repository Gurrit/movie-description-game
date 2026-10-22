package xyz.engsmyre.moviedescriptiongame.support

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

/**
 * A fake TMDB with [MOVIES] valid movies (ids 1..MOVIES), served as [PAGES] discover pages. Any other id is
 * unknown (404). Reused by the integration tests; the e2e setup can reuse the same stubs.
 */
object TmdbWireMock {
    const val PAGES = 10
    const val PER_PAGE = 20
    const val MOVIES = PAGES * PER_PAGE

    fun start(): WireMockServer {
        val server = WireMockServer(options().dynamicPort())
        server.start()
        for (page in 1..PAGES) {
            val results = ((page - 1) * PER_PAGE + 1..page * PER_PAGE).joinToString(",") {
                """{"id":$it,"title":"Movie $it Title","release_date":"${1990 + it % 30}-06-01"}"""
            }
            server.stubFor(
                get(urlPathEqualTo("/3/discover/movie")).withQueryParam("page", equalTo("$page"))
                    .willReturn(json("""{"page":$page,"total_pages":$PAGES,"results":[$results]}"""))
            )
        }
        for (id in 1..MOVIES) {
            server.stubFor(get(urlPathEqualTo("/3/movie/$id")).willReturn(json(Fixtures.detailsJson(Fixtures.details(id)))))
        }
        server.stubFor(
            get(urlPathEqualTo("/3/search/movie")).willReturn(
                json("""{"results":[{"id":1,"title":"Movie 1 Title","release_date":"1991-06-01"},{"id":2,"title":"Movie 2 Title","release_date":"1992-06-01"}]}""")
            )
        )
        return server
    }

    private fun json(body: String) = aResponse().withHeader("Content-Type", "application/json").withBody(body)
}
