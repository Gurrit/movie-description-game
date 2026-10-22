package xyz.engsmyre.moviedescriptiongame.api.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.SessionApi
import xyz.engsmyre.moviedescriptiongame.api.mapper.SessionMapper
import xyz.engsmyre.moviedescriptiongame.api.model.SessionRequest
import xyz.engsmyre.moviedescriptiongame.api.model.SessionResponse
import xyz.engsmyre.moviedescriptiongame.service.SessionService

@RestController
class SessionController(
    private val sessionService: SessionService,
    private val sessionMapper: SessionMapper
) : SessionApi {

    override suspend fun createSession(sessionRequest: SessionRequest?): ResponseEntity<SessionResponse> {
        val existing = sessionRequest?.sessionId?.let { id ->
            withContext(Dispatchers.IO) { sessionService.getSession(id) }
        }
        if (existing != null) {
            return ResponseEntity.ok(sessionMapper.toResponse(existing))
        }
        val created = withContext(Dispatchers.IO) { sessionService.createSession() }
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toResponse(created))
    }

    override suspend fun getSession(sessionId: String): ResponseEntity<SessionResponse> {
        val session = withContext(Dispatchers.IO) { sessionService.getSession(sessionId) }
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(sessionMapper.toResponse(session))
    }
}
