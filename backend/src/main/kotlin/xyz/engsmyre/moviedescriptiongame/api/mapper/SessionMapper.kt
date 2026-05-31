package xyz.engsmyre.moviedescriptiongame.api.mapper

import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueType
import xyz.engsmyre.moviedescriptiongame.api.dto.SessionResponse
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity

@Component
class SessionMapper {

    fun toResponse(session: SessionEntity): SessionResponse {
        return SessionResponse(
            sessionId = session.sessionId,
            challengeId = session.challengeId.toHexString(),
            challengeDate = session.challengeDate,
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
