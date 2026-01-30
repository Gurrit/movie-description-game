package xyz.engsmyre.moviedescriptiongame.db.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document(collection = "clue")
data class ScoreEntity (

    @Id
    var id: ObjectId,

    @Field(SESSION_ID_KEY)
    var sessionId: String,

    @Field(GIVEN_CLUES_KEY)
    var givenClues: Map<Int, String>,

    @Field(GUESSES_KEY)
    var guesses: Map<Int, String>

) {
    companion object {
        const val SESSION_ID_KEY = "sessionId"
        const val GIVEN_CLUES_KEY = "givenClues"
        const val GUESSES_KEY = "guesses"
    }
}