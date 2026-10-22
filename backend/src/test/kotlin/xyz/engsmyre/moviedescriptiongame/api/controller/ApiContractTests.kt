package xyz.engsmyre.moviedescriptiongame.api.controller

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.test.web.reactive.server.WebTestClient
import xyz.engsmyre.moviedescriptiongame.api.mapper.SessionMapper
import xyz.engsmyre.moviedescriptiongame.api.model.ClueResponse
import xyz.engsmyre.moviedescriptiongame.api.model.ClueType
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import xyz.engsmyre.moviedescriptiongame.service.ClueService
import xyz.engsmyre.moviedescriptiongame.service.SessionService
import org.bson.types.ObjectId

/** Checks that the controllers honour the contract in api/openapi.yaml (status codes, enums, bodies). */
class ApiContractTests {

    private val clueService = mock(ClueService::class.java)
    private val sessionService = mock(SessionService::class.java)

    private val client = WebTestClient
        .bindToController(ClueController(clueService), SessionController(sessionService, SessionMapper()))
        .controllerAdvice(ErrorHandlerController())
        .build()

    @Test
    fun `clue endpoint accepts a ClueType path value and returns the clue`() {
        `when`(clueService.getClue("s1", ClueType.YEAR))
            .thenReturn(ClueResponse(clue = "2010", clueType = ClueType.YEAR, remainingScore = 950, cluesUsed = listOf(ClueType.YEAR), currentMovieIndex = 0))

        client.post().uri("/clue/YEAR")
            .bodyValue(mapOf("sessionId" to "s1"))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.clue").isEqualTo("2010")
            .jsonPath("$.clueType").isEqualTo("YEAR")
            .jsonPath("$.cluesUsed[0]").isEqualTo("YEAR")
    }

    @Test
    fun `clue endpoint rejects an unknown clue type with 400`() {
        client.post().uri("/clue/BOGUS")
            .bodyValue(mapOf("sessionId" to "s1"))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `session endpoint returns 201 for a new session and 200 when resuming`() {
        val entity = SessionEntity(sessionId = "s1", challengeId = ObjectId.get(), challengeDate = "2026-05-31")
        `when`(sessionService.createSession()).thenReturn(entity)
        `when`(sessionService.getSession("s1")).thenReturn(entity)

        client.post().uri("/session")
            .exchange()
            .expectStatus().isCreated
            .expectBody().jsonPath("$.challengeDate").isEqualTo("2026-05-31")

        client.post().uri("/session")
            .bodyValue(mapOf("sessionId" to "s1"))
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `unknown session returns 404`() {
        client.get().uri("/session/nope").exchange().expectStatus().isNotFound
    }
}
