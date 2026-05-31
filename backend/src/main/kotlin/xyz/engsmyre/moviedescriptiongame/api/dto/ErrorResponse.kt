package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response")
data class ErrorResponse(
    @Schema(description = "Error code", example = "NOT_FOUND")
    val error: String,
    @Schema(description = "Human-readable error message", example = "No challenge found for the specified date")
    val message: String,
    @Schema(description = "Additional error details")
    val details: Map<String, Any>? = null
)
