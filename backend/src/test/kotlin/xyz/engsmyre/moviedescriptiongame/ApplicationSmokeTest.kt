package xyz.engsmyre.moviedescriptiongame

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgresTestConfig::class)
class ApplicationSmokeTest(
    @Autowired private val db: DatabaseClient,
    @Autowired private val client: WebTestClient,
) {
    @Test
    fun `migrations create the schema`() = runTest {
        val tables = db.sql(
            "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN " +
                "('players','daily_sets','daily_set_movies','games','game_rounds','guesses','revealed_clues')"
        ).map { row -> row.get(0, java.lang.Long::class.java)!!.toLong() }.one().awaitSingle()
        assertEquals(7L, tables)
    }

    @Test
    fun `liveness and readiness probes are up`() {
        client.get().uri("/healthz/liveness").exchange().expectStatus().isOk
        client.get().uri("/healthz/readiness").exchange().expectStatus().isOk
    }
}
