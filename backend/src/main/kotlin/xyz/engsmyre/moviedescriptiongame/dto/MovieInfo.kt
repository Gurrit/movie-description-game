package xyz.engsmyre.moviedescriptiongame.dto

import tools.jackson.databind.annotation.JsonSerialize

@JsonSerialize
data class MovieInfo(
    val description: String?,
    val leadActor: String?,
    val director: String?
)