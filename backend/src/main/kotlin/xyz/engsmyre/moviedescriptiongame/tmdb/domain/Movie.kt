package xyz.engsmyre.moviedescriptiongame.tmdb.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.time.LocalDate

class Movie internal constructor() : Serializable {
    @JsonProperty("id")
    var id = 0

    @JsonProperty("vote_count")
    private var nVotes = 0

    @JsonProperty("vote_average")
    var rating = 0.0

    @JsonProperty("original_title")
    var originalTitle: String? = null

    @JsonProperty("title")
    var primaryTitle: String? = null
        get() = field

    @JsonProperty("overview")
    var description: String? = null

    @JsonProperty("release_date")
    var releaseDate: LocalDate? = null

    @JsonProperty("")
    var coverArtPath : String? = null


    fun getnVotes(): Int {
        return nVotes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Movie

        if (id != other.id) return false
        if (nVotes != other.nVotes) return false
        if (rating != other.rating) return false
        if (originalTitle != other.originalTitle) return false
        if (description != other.description) return false
        if (releaseDate != other.releaseDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + nVotes
        result = 31 * result + rating.hashCode()
        result = 31 * result + (originalTitle?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Movie(id=$id, nVotes=$nVotes, rating=$rating, originalTitle=$originalTitle, primaryTitle=$primaryTitle, description=$description, releaseDate=$releaseDate)"
    }

}