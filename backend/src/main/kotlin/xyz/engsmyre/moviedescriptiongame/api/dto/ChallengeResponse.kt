package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Daily challenge response")
data class ChallengeResponse(
    @Schema(description = "Challenge unique identifier", example = "60d5ec9f8b3a3e2b4c8b4567")
    val id: String,
    @Schema(description = "Date of the challenge", example = "2026-05-31")
    val date: LocalDate,
    @Schema(description = "List of movies in this challenge")
    val movies: List<MovieInfo>
)
