package xyz.engsmyre.moviedescriptiongame.service

import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.api.dto.ClueType
import xyz.engsmyre.moviedescriptiongame.api.dto.GuessResponse
import xyz.engsmyre.moviedescriptiongame.api.dto.MovieInfo
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.SessionEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import java.time.LocalDate

@Service
class GuessService(
    private val challengeRepository: ChallengeRepository,
    private val sessionService: SessionService
) {

    fun submitGuess(sessionId: String, movieId: Int): GuessResponse {
        // Get session
        val session = sessionService.getSession(sessionId)
            ?: throw IllegalArgumentException("Session not found: $sessionId")

        // Get current challenge
        val challenge = challengeRepository.getChallenge(LocalDate.parse(session.challengeDate))
            ?: throw IllegalStateException("Challenge not found for date: ${session.challengeDate}")

        // Check if movie is in challenge
        val movieIndex = challenge.movies.indexOfFirst { it.tmdbId == movieId }
        
        if (movieIndex == -1) {
            // Movie not in challenge
            return GuessResponse(
                correct = false,
                score = 0,
                movie = null,
                remainingMovies = getRemainingMovies(challenge, session),
                totalScore = session.currentScore,
                message = "Incorrect guess. Try again!"
            )
        }

        // Check if already guessed
        if (movieId in session.guessedMovies) {
            return GuessResponse(
                correct = false,
                score = 0,
                movie = null,
                remainingMovies = getRemainingMovies(challenge, session),
                totalScore = session.currentScore,
                message = "You already guessed this movie!"
            )
        }

        // Correct guess!
        val movie = challenge.movies[movieIndex]
        val score = calculateScore(session, movieIndex)
        
        // Update session
        val updatedSession = updateSessionWithGuess(session, movieId, movieIndex, score)
        sessionService.updateSession(updatedSession)

        // Check if all movies guessed
        val allGuessed = updatedSession.correctGuesses.size == challenge.movies.size
        
        return GuessResponse(
            correct = true,
            score = score,
            movie = MovieInfo(
                id = movie.tmdbId,
                title = movie.movieTitle,
                originalTitle = movie.movieOriginalTitle,
                year = movie.year,
                directors = movie.directors,
                genres = movie.genres,
                posterUrl = movie.coverArtUrl,
                description = movie.description
            ),
            remainingMovies = getRemainingMovies(challenge, updatedSession),
            totalScore = updatedSession.currentScore,
            message = if (allGuessed) "Congratulations! You guessed all movies!" 
                     else "Correct! That was ${movie.movieTitle}"
        )
    }

    private fun calculateScore(session: SessionEntity, movieIndex: Int): Int {
        // Base score per movie
        val baseScore = 1000
        
        // Get clues used for this movie
        val cluesUsed = session.cluesUsed[movieIndex] ?: emptyList()
        
        // Sum up point costs of all clues used for this movie
        val clueCost = cluesUsed.sumOf { clueTypeName ->
            try {
                ClueType.valueOf(clueTypeName).pointCost
            } catch (e: IllegalArgumentException) {
                0
            }
        }
        
        // Score is base minus clue costs, minimum 0
        return maxOf(baseScore - clueCost, 0)
    }

    private fun updateSessionWithGuess(
        session: SessionEntity,
        movieId: Int,
        movieIndex: Int,
        score: Int
    ): SessionEntity {
        // Add to guessed and correct lists
        val updatedGuessed = (session.guessedMovies + movieId).distinct()
        val updatedCorrect = (session.correctGuesses + movieId).distinct()
        
        // Add score
        val newScore = session.currentScore + score
        
        // Move to next movie if not all guessed
        // If all 5 movies are correctly guessed, stay on current index
        // Otherwise, move to next movie
        val newMovieIndex = if (updatedCorrect.size >= 5) {
            session.currentMovieIndex  // Stay on current if all done
        } else {
            movieIndex + 1
        }
        
        return session.copy(
            currentScore = newScore,
            guessedMovies = updatedGuessed,
            correctGuesses = updatedCorrect,
            currentMovieIndex = newMovieIndex,
            updatedAt = java.time.Instant.now()
        )
    }

    private fun getRemainingMovies(
        challenge: ChallengeEntity,
        session: SessionEntity
    ): List<Int> {
        return challenge.movies
            .filter { it.tmdbId !in session.correctGuesses }
            .map { it.tmdbId }
    }
}
