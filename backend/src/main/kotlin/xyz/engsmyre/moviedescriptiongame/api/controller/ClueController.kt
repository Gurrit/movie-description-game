package xyz.engsmyre.moviedescriptiongame.api.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.ClueApi
import xyz.engsmyre.moviedescriptiongame.api.model.ClueRequest
import xyz.engsmyre.moviedescriptiongame.api.model.ClueResponse
import xyz.engsmyre.moviedescriptiongame.api.model.ClueType
import xyz.engsmyre.moviedescriptiongame.service.ClueService

@RestController
class ClueController(
    private val clueService: ClueService
) : ClueApi {

    override suspend fun getClue(type: ClueType, clueRequest: ClueRequest): ResponseEntity<ClueResponse> {
        val response = withContext(Dispatchers.IO) { clueService.getClue(clueRequest.sessionId, type) }
        return ResponseEntity.ok(response)
    }
}
