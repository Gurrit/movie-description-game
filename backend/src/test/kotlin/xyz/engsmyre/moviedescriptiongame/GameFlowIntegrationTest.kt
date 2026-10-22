package xyz.engsmyre.moviedescriptiongame

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.json.JsonMapper
import xyz.engsmyre.moviedescriptiongame.support.ApiClient
import xyz.engsmyre.moviedescriptiongame.support.ApiResult
import xyz.engsmyre.moviedescriptiongame.support.INTERNAL_TOKEN
import xyz.engsmyre.moviedescriptiongame.support.IntegrationTestConfig
import xyz.engsmyre.moviedescriptiongame.support.TmdbWireMock
import java.util.concurrent.Executors
import java.util.concurrent.Future

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "30s")
@Import(IntegrationTestConfig::class)
class GameFlowIntegrationTest(
    @Autowired web: WebTestClient,
    @Autowired json: JsonMapper,
    @Autowired private val db: DatabaseClient,
) {
    private val api = ApiClient(web, json)

    /** The correct TMDB ids of today's game in round order (read from the database; the API hides them). */
    private suspend fun answers(): List<Int> = db.sql(
        "SELECT gm.movie_id FROM game_movie gm JOIN daily_game dg ON dg.game_id = gm.game_id ORDER BY dg.date, gm.position"
    ).map { r -> (r.get("movie_id", java.lang.Integer::class.java)!!).toInt() }.all().collectList().awaitSingle()

    private fun wrongGuessFor(answer: Int, offset: Int = 0) = (1..TmdbWireMock.MOVIES).filter { it != answer }[offset]

    private fun rounds(g: ApiResult) = (0 until g["rounds"].size()).map { g["rounds"][it] }

    @Test
    fun `endpoints other than creating a player need a token`() {
        for ((method, path) in listOf("GET" to "/game/today", "POST" to "/game/today", "GET" to "/players/me/stats")) {
            val r = api.call(method, path)
            assertEquals(401, r.status, "$method $path")
            assertEquals("INVALID_TOKEN", r.code)
        }
        assertEquals(401, api.call("GET", "/game/today", token = "not-a-real-token").status)
    }

    @Test
    fun `game starts with one active round and no clue content`() {
        val token = api.createPlayer()
        val notStarted = api.today(token)
        assertEquals(404, notStarted.status)
        assertEquals("GAME_NOT_STARTED", notStarted.code)

        val started = api.start(token)
        assertEquals(201, started.status)
        assertEquals("IN_PROGRESS", started["status"].asString())
        assertEquals("2026-10-22", started["date"].asString())
        assertEquals(7, started["roundsPerGame"].asInt())
        assertEquals(3, started["attemptsPerRound"].asInt())
        assertEquals(0, started["currentRound"].asInt())

        val rounds = rounds(started)
        assertEquals(7, rounds.size)
        assertEquals("ACTIVE", rounds[0]["status"].asString())
        assertTrue(rounds.drop(1).all { it["status"].asString() == "PENDING" && it["clues"].size() == 0 })
        assertEquals(listOf("plot", "tagline", "metadata", "people"), rounds[0]["clues"].map { it["id"].asString() })
        assertTrue(rounds[0]["clues"].none { it["revealed"].asBoolean() || it.has("content") })
        assertFalse(rounds[0].has("answer"))

        val again = api.start(token)
        assertEquals(200, again.status)
        assertEquals(started["id"], again["id"])
        assertEquals(started["id"], api.today(token)["id"])
    }

    @Test
    fun `revealing a clue shows its content and is idempotent`() {
        val token = api.createPlayer().also { api.start(it) }

        val first = api.reveal(token, "tagline")
        assertEquals(200, first.status)
        val clue = rounds(first)[0]["clues"].single { it["id"].asString() == "tagline" }
        assertTrue(clue["revealed"].asBoolean())
        assertTrue(clue["content"]["text"].asString().isNotBlank())
        assertEquals(0, rounds(first)[0]["attemptsUsed"].asInt(), "revealing costs nothing")

        assertEquals(200, api.reveal(token, "tagline").status)
        val people = rounds(api.reveal(token, "people"))[0]["clues"].single { it["id"].asString() == "people" }
        assertEquals(5, people["content"]["people"].size())

        val unknown = api.reveal(token, "nope")
        assertEquals(404, unknown.status)
        assertEquals("CLUE_NOT_FOUND", unknown.code)
    }

    @Test
    fun `clue text never contains the movie title`() = runTest {
        val token = api.createPlayer().also { api.start(it) }
        for (id in listOf("plot", "tagline")) api.reveal(token, id)
        val round = rounds(api.today(token))[0]
        // titles are "Movie <id> Title", the overview and tagline of the fixtures never mention them,
        // but the masks must not have produced garbage either
        for (clue in round["clues"].filter { it["revealed"].asBoolean() }) {
            assertFalse(clue["content"]["text"].asString().contains("Movie ", ignoreCase = true))
        }
        assertTrue(answers().size == 7)
    }

    @Test
    fun `a full game with a solve, a failure and a finish`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }

        // Unknown movies are rejected without costing an attempt
        val unknown = api.guess(token, 99_999)
        assertEquals(422, unknown.status)
        assertEquals("VALIDATION_FAILED", unknown.code)

        // Round 0: one wrong guess, a duplicate of it, then the right one
        val wrong = wrongGuessFor(answers[0])
        val afterWrong = api.guess(token, wrong)
        assertEquals(200, afterWrong.status)
        val r0 = rounds(afterWrong)[0]
        assertEquals(1, r0["attemptsUsed"].asInt())
        assertEquals("ACTIVE", r0["status"].asString())
        assertFalse(r0["guesses"][0]["correct"].asBoolean())
        assertFalse(r0.has("answer"))

        val duplicate = api.guess(token, wrong)
        assertEquals(409, duplicate.status)
        assertEquals("ALREADY_GUESSED", duplicate.code)
        assertEquals(1, rounds(api.today(token))[0]["attemptsUsed"].asInt(), "a duplicate costs nothing")

        val solved = api.guess(token, answers[0])
        assertEquals("SOLVED", rounds(solved)[0]["status"].asString())
        assertEquals(answers[0], rounds(solved)[0]["answer"]["tmdbId"].asInt())
        assertEquals(2, rounds(solved)[0]["attemptsUsed"].asInt())
        assertEquals(1, solved["currentRound"].asInt())
        assertEquals("ACTIVE", rounds(solved)[1]["status"].asString())
        assertTrue(rounds(solved)[0]["clues"].all { it.has("content") }, "ended rounds show all clues")

        // Round 1: three wrong guesses fail the round; attempts do not carry over
        var state = solved
        for (i in 0 until 3) state = api.guess(token, wrongGuessFor(answers[1], i))
        assertEquals("FAILED", rounds(state)[1]["status"].asString())
        assertEquals(answers[1], rounds(state)[1]["answer"]["tmdbId"].asInt())
        assertEquals(2, state["currentRound"].asInt())
        assertEquals(0, rounds(state)[2]["attemptsUsed"].asInt())

        // Rounds 2..6: solved at first attempt each
        for (round in 2..6) state = api.guess(token, answers[round])
        assertEquals("COMPLETE", state["status"].asString())
        assertFalse(state.has("currentRound") && !state["currentRound"].isNull)
        assertEquals(listOf("SOLVED", "FAILED"), rounds(state).take(2).map { it["status"].asString() })

        // A finished game is read-only
        assertEquals("GAME_COMPLETE", api.guess(token, answers[0]).code)
        assertEquals("GAME_COMPLETE", api.reveal(token, "plot").code)
        assertEquals("COMPLETE", api.today(token)["status"].asString())
        assertEquals(200, api.start(token).status)

        val stats = api.stats(token)
        assertEquals(1, stats["gamesPlayed"].asInt())
        assertEquals(0, stats["gamesFullySolved"].asInt())
        assertEquals(6, stats["moviesSolved"].asInt())
    }

    @Test
    fun `a flawless game counts as fully solved`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }
        var state = api.today(token)
        for (answer in answers) state = api.guess(token, answer)
        assertEquals("COMPLETE", state["status"].asString())

        val stats = api.stats(token)
        assertEquals(1, stats["gamesPlayed"].asInt())
        assertEquals(1, stats["gamesFullySolved"].asInt())
        assertEquals(7, stats["moviesSolved"].asInt())
    }

    @Test
    fun `stats start at zero and unfinished games do not count`() {
        val token = api.createPlayer()
        assertEquals(0, api.stats(token)["gamesPlayed"].asInt())
        api.start(token)
        val stats = api.stats(token)
        assertEquals(0, stats["gamesPlayed"].asInt())
        assertEquals(0, stats["moviesSolved"].asInt())
    }

    @Test
    fun `all players share the same game`() = runTest {
        val a = api.createPlayer().also { api.start(it) }
        val b = api.createPlayer().also { api.start(it) }
        val games = db.sql("SELECT DISTINCT game_id FROM playthrough").map { r -> r.get("game_id", java.util.UUID::class.java)!! }
            .all().collectList().awaitSingle()
        assertEquals(1, games.size, "one game per date")
        assertNotEquals(api.today(a)["id"], api.today(b)["id"], "but separate playthroughs")
        val gamesForDate = db.sql("SELECT count(*) AS n FROM daily_game WHERE date = DATE '2026-10-22'").map { r -> (r.get("n", java.lang.Long::class.java)!!).toInt() }.one().awaitSingle()
        assertEquals(1, gamesForDate)
    }

    @Test
    fun `concurrent identical guesses use one attempt`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }
        val wrong = wrongGuessFor(answers[0])

        val pool = Executors.newFixedThreadPool(4)
        val results: List<Future<ApiResult>> = (1..4).map { pool.submit<ApiResult> { api.guess(token, wrong) } }
        val statuses = results.map { it.get().status }.sorted()
        pool.shutdown()

        assertEquals(1, statuses.count { it == 200 }, "exactly one wins: $statuses")
        assertEquals(3, statuses.count { it == 409 }, statuses.toString())
        assertEquals(1, rounds(api.today(token))[0]["attemptsUsed"].asInt())
    }

    @Test
    fun `movie search is validated and proxied`() {
        val token = api.createPlayer()
        val found = api.call("GET", "/movies/search", token, query = mapOf("q" to "movie"))
        assertEquals(200, found.status)
        assertEquals(2, found.body.size())
        assertEquals("Movie 1 Title", found.body[0]["title"].asString())
        assertEquals(1991, found.body[0]["year"].asInt())

        // the request itself breaks the contract on purpose, so only the response is checked by hand
        val tooShort = api.call("GET", "/movies/search", token, query = mapOf("q" to "a"), validate = false)
        assertEquals(422, tooShort.status)
        assertEquals("VALIDATION_FAILED", tooShort.code)
        assertEquals(401, api.call("GET", "/movies/search", query = mapOf("q" to "movie")).status)
    }

    @Test
    fun `the internal generate endpoint is protected and idempotent`() = runTest {
        api.start(api.createPlayer())
        val noToken = api.call("POST", "/internal/generate", validate = false)
        assertEquals(401, noToken.status)

        val wrongToken = api.web.post().uri("/internal/generate").header("X-Internal-Token", "wrong").exchange().returnResult(String::class.java)
        assertEquals(401, wrongToken.status.value())

        val ok = api.web.post().uri("/internal/generate").header("X-Internal-Token", INTERNAL_TOKEN)
            .exchange().expectStatus().isOk.expectBody(String::class.java).returnResult().responseBody!!
        assertTrue(answers().all { ok.contains(it.toString()) }, ok)

        api.web.post().uri("/internal/generate?date=2026-10-23").header("X-Internal-Token", INTERNAL_TOKEN)
            .exchange().expectStatus().isOk
        api.web.post().uri("/internal/generate?date=2026-12-31").header("X-Internal-Token", INTERNAL_TOKEN)
            .exchange().expectStatus().isEqualTo(422)
    }
}
