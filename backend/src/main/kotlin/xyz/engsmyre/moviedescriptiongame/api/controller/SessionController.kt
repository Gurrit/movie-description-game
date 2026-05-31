package xyz.engsmyre.moviedescriptiongame.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.dto.SessionResponse
import xyz.engsmyre.moviedescriptiongame.api.mapper.SessionMapper
import xyz.engsmyre.moviedescriptiongame.service.SessionService

@Tag(name = "Session", description = "Manage user game sessions")
@RestController
@RequestMapping("/api/session")
class SessionController(
    private val sessionService: SessionService,
    private val sessionMapper: SessionMapper
) {

    @Operation(
        summary = "Create or get a session",
        description = "Creates a new game session or returns an existing one for the current day's challenge"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Session details"),
            ApiResponse(responseCode = "201", description = "New session created")
        ]
    )
    @PostMapping
    fun createOrGetSession(
        @RequestBody
        sessionRequest: SessionRequest
    ): ResponseEntity<SessionResponse> {
        val session = if (sessionRequest.sessionId != null) {
            sessionService.getSession(sessionRequest.sessionId)
                ?: sessionService.createSession()
        } else {
            sessionService.createSession()
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionMapper.toResponse(session))
    }

    @Operation(
        summary = "Get session state",
        description = "Returns the current state of a game session"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Session state"),
            ApiResponse(responseCode = "404", description = "Session not found", content = [Content(mediaType = "application/json")])
        ]
    )
    @GetMapping("/{sessionId}")
    fun getSession(
        @Parameter(description = "Session ID")
        @PathVariable
        sessionId: String
    ): ResponseEntity<SessionResponse> {
        val session = sessionService.getSession(sessionId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(sessionMapper.toResponse(session))
    }
}

// DTO for session creation/resume
data class SessionRequest(
    val sessionId: String? = null
)
