package xyz.engsmyre.moviedescriptiongame.db.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDate

@Document(collection = "challenge")
// TODO Finish this
data class ChallengeEntity (

    @Id
    var id: ObjectId,

    @Field(MOVIE_NAME_KEY)
    var movies: List<MovieEntity>,

    @Field(DAY_KEY)
    var day: String
) {
    companion object {
        const val MOVIE_NAME_KEY = "movieName"
        const val DAY_KEY = "day"
    }
}
