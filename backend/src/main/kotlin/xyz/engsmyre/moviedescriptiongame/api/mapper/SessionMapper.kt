package xyz.engsmyre.moviedescriptiongame.api.mapper

import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.api.model.ClueType
import xyz.engsmyre.moviedescriptiongame.api.model.SessionResponse
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import java.time.LocalDate

@Component
class SessionMapper {

    fun toResponse(session: SessionEntity): SessionResponse {
        return SessionResponse(
            sessionId = session.sessionId,
            challengeId = session.challengeId.toHexString(),
            challengeDate = LocalDate.parse(session.challengeDate),
            currentScore = session.currentScore,
            cluesUsed = session.cluesUsed.values.flatten().mapNotNull {
                try { ClueType.valueOf(it) } catch (e: IllegalArgumentException) { null }
            },
            guessedMovies = session.guessedMovies,
            correctGuesses = session.correctGuesses,
            currentMovieIndex = session.currentMovieIndex,
            startedAt = session.startedAt,
            updatedAt = session.updatedAt
        )
    }
}
