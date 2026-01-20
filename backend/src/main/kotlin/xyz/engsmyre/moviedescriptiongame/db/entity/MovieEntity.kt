package xyz.engsmyre.moviedescriptiongame.db.entity

import org.springframework.data.mongodb.core.mapping.Field

data class MovieEntity (
    @Field(MOVIE_NAME_KEY)
    var movieName: String
) {
    companion object {
        const val MOVIE_NAME_KEY = "movieName"
    }
}