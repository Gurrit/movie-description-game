package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Game session state")
data class SessionResponse(
    @Schema(description = "Unique session identifier", example = "abc123-def456-ghi789")
    val sessionId: String,
    @Schema(description = "ID of the challenge being played", example = "60d5ec9f8b3a3e2b4c8b4567")
    val challengeId: String,
    @Schema(description = "Date of the challenge", example = "2026-05-31")
    val challengeDate: String,
    @Schema(description = "Current score for the session", example = "1500")
    val currentScore: Int = 0,
    @Schema(description = "List of clue types used so far")
    val cluesUsed: List<ClueType> = emptyList(),
    @Schema(description = "List of guessed movie IDs")
    val guessedMovies: List<Int> = emptyList(),
    @Schema(description = "List of correctly guessed movie IDs")
    val correctGuesses: List<Int> = emptyList(),
    @Schema(description = "Index of the current movie being guessed (0-4)", example = "0")
    val currentMovieIndex: Int = 0,
    @Schema(description = "When the session was started")
    val startedAt: Instant? = null,
    @Schema(description = "When the session was last updated")
    val updatedAt: Instant? = null
)
