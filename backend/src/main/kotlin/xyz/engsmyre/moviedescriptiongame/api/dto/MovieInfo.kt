package xyz.engsmyre.moviedescriptiongame.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Movie information for API responses")
data class MovieInfo(
    @Schema(description = "TMDB movie ID", example = "27205")
    val id: Int,
    @Schema(description = "Movie title", example = "Inception")
    val title: String,
    @Schema(description = "Original movie title", example = "Inception")
    val originalTitle: String? = null,
    @Schema(description = "Release year", example = "2010")
    val year: String,
    @Schema(description = "List of directors")
    val directors: List<String> = emptyList(),
    @Schema(description = "List of genres")
    val genres: List<String> = emptyList(),
    @Schema(description = "URL to movie poster")
    val posterUrl: String? = null,
    @Schema(description = "Movie description/plot")
    val description: String? = null
)
