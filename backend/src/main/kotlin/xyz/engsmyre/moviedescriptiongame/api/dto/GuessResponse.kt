package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response when submitting a guess")
data class GuessResponse(
    @Schema(description = "Whether the guess was correct", example = "true")
    val correct: Boolean,
    @Schema(description = "Points earned from this guess (including clue penalties)", example = "850")
    val score: Int,
    @Schema(description = "The movie details (only present if correct)")
    val movie: MovieInfo? = null,
    @Schema(description = "IDs of movies not yet guessed")
    val remainingMovies: List<Int> = emptyList(),
    @Schema(description = "Cumulative score for all guessed movies", example = "1850")
    val totalScore: Int = 0,
    @Schema(description = "Feedback message", example = "Correct! That was Inception (2010)")
    val message: String? = null
)
