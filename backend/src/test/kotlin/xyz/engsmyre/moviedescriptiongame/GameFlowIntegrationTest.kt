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
        assertEquals(7, started["attemptsPerRound"].asInt())
        assertEquals(0, started["currentRound"].asInt())

        val rounds = rounds(started)
        assertEquals(7, rounds.size)
        assertEquals("ACTIVE", rounds[0]["status"].asString())
        assertTrue(rounds.drop(1).all { it["status"].asString() == "PENDING" && it["clues"].size() == 0 })
        assertEquals(
            listOf("metadata", "writer", "studio", "director", "cast", "tagline", "plot"),
            rounds[0]["clues"].map { it["id"].asString() },
        )
        assertEquals(listOf(true, false, false, false, false, false, false), rounds[0]["clues"].map { it["revealed"].asBoolean() })
        assertEquals(listOf(true, false, false, false, false, false, false), rounds[0]["clues"].map { it.has("content") })
        assertFalse(rounds[0].has("answer"))

        val again = api.start(token)
        assertEquals(200, again.status)
        assertEquals(started["id"], again["id"])
        assertEquals(started["id"], api.today(token)["id"])
    }

    @Test
    fun `each wrong guess unlocks the next clue, and the reveal endpoint is gone`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }
        val order = listOf("metadata", "writer", "studio", "director", "cast", "tagline", "plot")

        fun unlocked(game: ApiResult) = rounds(game)[0]["clues"].filter { it.has("content") }.map { it["id"].asString() }

        var state = api.today(token)
        assertEquals(order.take(1), unlocked(state))
        for (wrong in 1..6) {
            state = api.guess(token, wrongGuessFor(answers[0], wrong))
            assertEquals(order.take(wrong + 1), unlocked(state), "after $wrong wrong guesses")
        }

        val content = rounds(state)[0]["clues"].associate { it["id"].asString() to it["content"] }
        assertTrue(content.getValue("metadata")["genres"].size() > 0)
        assertEquals(0, content.getValue("metadata")["decade"].asInt() % 10)
        assertFalse(content.getValue("metadata").has("releaseYear"), "the exact year stays on the server")
        assertEquals(1, content.getValue("writer")["names"].size())
        assertEquals(1, content.getValue("studio")["names"].size())
        assertEquals(1, content.getValue("director")["names"].size())
        assertEquals(3, content.getValue("cast")["names"].size())
        assertTrue(content.getValue("tagline")["text"].asString().isNotBlank())
        assertTrue(content.getValue("plot")["text"].asString().isNotBlank())
        assertEquals(listOf("Hero", "Sidekick", "Villain", "Bystander").filter { content.toString().contains(it) }, emptyList<String>(), "no characters")

        assertEquals(404, api.call("POST", "/game/today/rounds/current/clues/plot/reveal", token, validate = false).status)
    }

    @Test
    fun `skipping uses an attempt and unlocks the next clue`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }
        fun unlocked(game: ApiResult) = rounds(game)[0]["clues"].count { it.has("content") }

        val skipped = api.skip(token)
        assertEquals(200, skipped.status)
        assertEquals(1, rounds(skipped)[0]["attemptsUsed"].asInt())
        assertEquals(0, rounds(skipped)[0]["guesses"].size())
        assertEquals(2, unlocked(skipped))

        // a guess after a skip is the second attempt and unlocks one more
        val guessed = api.guess(token, wrongGuessFor(answers[0]))
        assertEquals(2, rounds(guessed)[0]["attemptsUsed"].asInt())
        assertEquals(3, unlocked(guessed))
        assertEquals(4, unlocked(api.skip(token)))
        assertEquals(3, rounds(api.today(token))[0]["attemptsUsed"].asInt(), "reload keeps the skips")
    }

    @Test
    fun `skipping the last attempt fails the round and moves on`() = runTest {
        val token = api.createPlayer().also { api.start(it) }
        var state = api.today(token)
        for (i in 1..6) {
            state = api.skip(token)
            assertEquals("ACTIVE", rounds(state)[0]["status"].asString())
        }
        state = api.skip(token)
        assertEquals("FAILED", rounds(state)[0]["status"].asString())
        assertEquals(7, rounds(state)[0]["attemptsUsed"].asInt())
        assertTrue(rounds(state)[0].has("answer"))
        assertEquals(1, state["currentRound"].asInt())
        assertEquals(0, rounds(state)[1]["attemptsUsed"].asInt())
        assertEquals(1, rounds(state)[1]["clues"].count { it.has("content") })
    }

    @Test
    fun `skip needs a started game and an unfinished one`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer()
        assertEquals("GAME_NOT_STARTED", api.skip(token).code)
        assertEquals(401, api.call("POST", "/game/today/rounds/current/skip").status)

        api.start(token)
        for (answer in answers) api.guess(token, answer)
        assertEquals("GAME_COMPLETE", api.skip(token).code)
    }

    @Test
    fun `a correct guess unlocks nothing and shows every clue of the round`() = runTest {
        val answers = answers().ifEmpty { api.start(api.createPlayer()); answers() }
        val token = api.createPlayer().also { api.start(it) }
        val solved = api.guess(token, answers[0])
        assertTrue(rounds(solved)[0]["clues"].all { it["revealed"].asBoolean() && it.has("content") })
        // the next round starts over with one clue
        assertEquals(1, rounds(solved)[1]["clues"].count { it["revealed"].asBoolean() })
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

        // Round 1: all seven attempts wrong fail the round; attempts do not carry over
        var state = solved
        for (i in 0 until 7) state = api.guess(token, wrongGuessFor(answers[1], i))
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
    fun `random movie comes from the pool and needs a token`() {
        val token = api.createPlayer()
        val movie = api.call("GET", "/movies/random", token)
        assertEquals(200, movie.status)
        assertTrue(movie["tmdbId"].asInt() in 1..TmdbWireMock.MOVIES)
        assertTrue(movie["title"].asString().isNotBlank())
        assertEquals(401, api.call("GET", "/movies/random").status)
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
