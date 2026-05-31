package xyz.engsmyre.moviedescriptiongame.service

import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueResponse
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueType
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.MovieSubEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import java.time.LocalDate

@Service
class ClueService(
    private val challengeRepository: ChallengeRepository,
    private val sessionService: SessionService
) {

    fun getClue(sessionId: String, clueType: ClueType): ClueResponse {
        // Get or create session
        val session = sessionService.getSession(sessionId)
            ?: throw IllegalArgumentException("Session not found: $sessionId")

        // Get current challenge
        val challenge = challengeRepository.getChallenge(LocalDate.parse(session.challengeDate))
            ?: throw IllegalStateException("Challenge not found for date: ${session.challengeDate}")

        // Get current movie
        val currentMovie = getCurrentMovie(challenge, session.currentMovieIndex)

        // Check if clue already used for this movie
        val movieIndex = session.currentMovieIndex
        val alreadyUsed = session.cluesUsed[movieIndex]?.contains(clueType.name) == true

        // Get the clue value based on type
        val clue = getClueValue(currentMovie, clueType)

        // Update session: only deduct points if clue not already used
        val updatedSession = if (alreadyUsed) {
            session  // Return unchanged session, no point deduction
        } else {
            updateSessionWithClue(session, clueType, clue)
        }
        
        if (!alreadyUsed) {
            sessionService.updateSession(updatedSession)
        }

        return ClueResponse(
            clue = clue,
            clueType = clueType,
            remainingScore = updatedSession.currentScore,
            cluesUsed = (updatedSession.cluesUsed[movieIndex] ?: emptyList())
                .mapNotNull { try { ClueType.valueOf(it) } catch (e: IllegalArgumentException) { null } },
            currentMovieIndex = updatedSession.currentMovieIndex
        )
    }

    private fun getCurrentMovie(challenge: ChallengeEntity, movieIndex: Int): MovieSubEntity {
        if (movieIndex < 0 || movieIndex >= challenge.movies.size) {
            throw IllegalArgumentException("Invalid movie index: $movieIndex")
        }
        return challenge.movies[movieIndex]
    }

    private fun getClueValue(movie: MovieSubEntity, clueType: ClueType): String {
        return when (clueType) {
            ClueType.YEAR -> movie.year
            ClueType.DIRECTOR -> movie.directors.firstOrNull() ?: "Unknown"
            ClueType.ACTOR_1 -> movie.billings[1] ?: "Unknown"
            ClueType.ACTOR_2 -> movie.billings[2] ?: "Unknown"
            ClueType.ACTOR_3 -> movie.billings[3] ?: "Unknown"
            ClueType.GENRE -> movie.genres.firstOrNull() ?: "Unknown"
            ClueType.PLOT_HINT -> generatePlotHint(movie)
            ClueType.TITLE_HINT -> movie.movieTitle.take(3) + "..."
        }
    }

    private fun generatePlotHint(movie: MovieSubEntity): String {
        val description = movie.description ?: ""
        if (description.isBlank()) {
            return "A movie about ${movie.movieTitle.take(10)}..."
        }
        
        val sentences = description.split(".", "!", "?")
        val firstSentence = sentences.firstOrNull()?.trim() ?: ""
        
        return if (firstSentence.isNotBlank()) {
            if (firstSentence.length > 50) {
                firstSentence.take(50) + "..."
            } else {
                firstSentence + "."
            }
        } else {
            description.split("\\s+".toRegex()).take(5).joinToString(" ") + "..."
        }
    }

    private fun updateSessionWithClue(
        session: SessionEntity,
        clueType: ClueType,
        clue: String
    ): SessionEntity {
        val movieIndex = session.currentMovieIndex
        
        // Deduct points
        val newScore = session.currentScore - clueType.pointCost
        
        // Update clues used for current movie
        val currentClues = session.cluesUsed[movieIndex] ?: emptyList()
        val updatedClues = (currentClues + clueType.name).distinct()
        val updatedCluesUsed = session.cluesUsed.toMutableMap().apply {
            put(movieIndex, updatedClues)
        }
        
        return session.copy(
            currentScore = newScore,
            cluesUsed = updatedCluesUsed,
            updatedAt = java.time.Instant.now()
        )
    }
}
