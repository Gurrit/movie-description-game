package xyz.engsmyre.moviedescriptiongame.api.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.GuessApi
import xyz.engsmyre.moviedescriptiongame.api.model.GuessRequest
import xyz.engsmyre.moviedescriptiongame.api.model.GuessResponse
import xyz.engsmyre.moviedescriptiongame.service.GuessService

@RestController
class GuessController(
    private val guessService: GuessService
) : GuessApi {

    override suspend fun submitGuess(guessRequest: GuessRequest): ResponseEntity<GuessResponse> {
        val response = withContext(Dispatchers.IO) {
            guessService.submitGuess(guessRequest.sessionId, guessRequest.movieId)
        }
        return ResponseEntity.ok(response)
    }
}
