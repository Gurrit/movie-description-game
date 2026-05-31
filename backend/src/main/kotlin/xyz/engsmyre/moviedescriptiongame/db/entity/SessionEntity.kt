package xyz.engsmyre.moviedescriptiongame.db.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "session")
data class SessionEntity(
    @Id
    var id: ObjectId = ObjectId.get(),

    @Field(SESSION_ID_KEY)
    var sessionId: String,

    @Field(CHALLENGE_ID_KEY)
    var challengeId: ObjectId,

    @Field(CHALLENGE_DATE_KEY)
    var challengeDate: String,

    @Field(CURRENT_SCORE_KEY)
    var currentScore: Int = STARTING_SCORE,

    @Field(CURRENT_MOVIE_INDEX_KEY)
    var currentMovieIndex: Int = 0,

    @Field(CLUES_USED_KEY)
    var cluesUsed: Map<Int, List<String>> = emptyMap(),  // movieIndex -> list of clue types

    @Field(GUESSED_MOVIES_KEY)
    var guessedMovies: List<Int> = emptyList(),  // TMDB IDs

    @Field(CORRECT_GUESSES_KEY)
    var correctGuesses: List<Int> = emptyList(),  // TMDB IDs

    @Field(STARTED_AT_KEY)
    var startedAt: Instant = Instant.now(),

    @Field(UPDATED_AT_KEY)
    var updatedAt: Instant = Instant.now()
) {
    companion object {
        const val STARTING_SCORE = 5000  // 1000 per movie * 5 movies
        
        const val SESSION_ID_KEY = "sessionId"
        const val CHALLENGE_ID_KEY = "challengeId"
        const val CHALLENGE_DATE_KEY = "challengeDate"
        const val CURRENT_SCORE_KEY = "currentScore"
        const val CURRENT_MOVIE_INDEX_KEY = "currentMovieIndex"
        const val CLUES_USED_KEY = "cluesUsed"
        const val GUESSED_MOVIES_KEY = "guessedMovies"
        const val CORRECT_GUESSES_KEY = "correctGuesses"
        const val STARTED_AT_KEY = "startedAt"
        const val UPDATED_AT_KEY = "updatedAt"
    }
}
