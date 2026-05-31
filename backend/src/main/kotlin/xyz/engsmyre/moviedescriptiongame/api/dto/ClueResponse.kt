package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response when requesting a clue")
data class ClueResponse(
    @Schema(description = "The clue value", example = "2010")
    val clue: String,
    @Schema(description = "Type of clue provided")
    val clueType: ClueType,
    @Schema(description = "Current score after clue deduction", example = "850")
    val remainingScore: Int,
    @Schema(description = "List of clue types already used in this session")
    val cluesUsed: List<ClueType> = emptyList(),
    @Schema(description = "Index of the current movie being guessed (0-4)", example = "0")
    val currentMovieIndex: Int = 0
)
