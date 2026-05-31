package xyz.engsmyre.moviedescriptiongame.tmdb.domain

import com.fasterxml.jackson.annotation.JsonProperty

class TmdbCreditsResponse {

    @JsonProperty("id")
    var id: Int = 0

    @JsonProperty("cast")
    var cast: List<CastMember>? = null

    @JsonProperty("crew")
    var crew: List<CrewMember>? = null

    data class CastMember(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String,
        @JsonProperty("character") val character: String? = null,
        @JsonProperty("order") val order: Int = 0
    )

    data class CrewMember(
        @JsonProperty("id") val id: Int,
        @JsonProperty("name") val name: String,
        @JsonProperty("job") val job: String,
        @JsonProperty("department") val department: String? = null
    )
}
