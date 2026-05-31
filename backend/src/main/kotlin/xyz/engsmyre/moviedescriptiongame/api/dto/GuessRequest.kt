package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to submit a guess")
data class GuessRequest(
    @Schema(description = "Unique session identifier", example = "abc123-def456-ghi789")
    val sessionId: String,
    @Schema(description = "TMDB movie ID being guessed", example = "12345")
    val movieId: Int
)
