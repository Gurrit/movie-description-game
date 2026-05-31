package xyz.engsmyre.moviedescriptiongame.tmdb.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

class TmdbMovieDetailsResponse {

    @JsonProperty("id")
    var id: Int = 0

    @JsonProperty("title")
    var title: String? = null

    @JsonProperty("original_title")
    var originalTitle: String? = null

    @JsonProperty("overview")
    var overview: String? = null

    @JsonProperty("release_date")
    var releaseDate: LocalDate? = null

    @JsonProperty("poster_path")
    var posterPath: String? = null

    @JsonProperty("backdrop_path")
    var backdropPath: String? = null

    @JsonProperty("vote_count")
    var voteCount: Int = 0

    @JsonProperty("vote_average")
    var voteAverage: Double = 0.0

    @JsonProperty("budget")
    var budget: Long = 0

    @JsonProperty("genres")
    var genres: List<Genre>? = null

    @JsonProperty("runtime")
    var runtime: Int? = null

    @JsonProperty("tagline")
    var tagline: String? = null

    data class Genre(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String
    )
}
