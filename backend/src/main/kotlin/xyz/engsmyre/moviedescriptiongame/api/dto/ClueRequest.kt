package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to get a clue")
data class ClueRequest(
    @Schema(description = "Unique session identifier", example = "abc123-def456-ghi789")
    val sessionId: String
)
