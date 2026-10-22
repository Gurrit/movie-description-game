package xyz.engsmyre.moviedescriptiongame.game

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import xyz.engsmyre.moviedescriptiongame.api.model.Game
import xyz.engsmyre.moviedescriptiongame.challenge.GameGenerator
import xyz.engsmyre.moviedescriptiongame.challenge.GameRepository
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbClient
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

class StartedGame(val game: Game, val created: Boolean)

/** The player's side of a game: starting it, revealing clues and guessing. */
@Service
class PlaythroughService(
    private val playthroughs: PlaythroughRepository,
    private val games: GameRepository,
    private val generator: GameGenerator,
    private val tmdb: TmdbClient,
    private val reveals: RevealPolicy,
    private val tx: TransactionalOperator,
    private val clock: Clock,
) {
    private fun today(): LocalDate = LocalDate.now(clock)

    suspend fun getToday(playerId: UUID): Game {
        val playthrough = playthroughs.findForDate(playerId, today()) ?: throw gameNotStarted()
        return view(playthrough.id)
    }

    suspend fun startToday(playerId: UUID): StartedGame {
        val game = generator.ensure(today())
        val existing = playthroughs.findByPlayerAndGame(playerId, game.id)
        if (existing != null) return StartedGame(view(existing.id), created = false)

        val fresh = Playthrough(UUID.randomUUID(), playerId, game.id, PlaythroughStatus.IN_PROGRESS, currentRound = 0)
        val created = tx.executeAndAwait { playthroughs.insert(fresh, game.roundsPerGame, clock.instant()) }!!
        // A concurrent start by the same player wins the insert; return theirs
        val id = if (created) fresh.id else playthroughs.findByPlayerAndGame(playerId, game.id)!!.id
        return StartedGame(view(id), created)
    }

    suspend fun reveal(playerId: UUID, clueId: String): Game {
        val playthrough = activePlaythrough(playerId)
        val id = tx.executeAndAwait {
            val locked = playthroughs.lock(playthrough.id)
            ensureInProgress(locked)
            val game = games.findById(locked.gameId)!!
            val movie = game.movies[locked.currentRound]
            if (movie.clues.none { it.id == clueId }) {
                throw ApiException(HttpStatus.NOT_FOUND, "CLUE_NOT_FOUND", "No such clue", "Unknown clue '$clueId'")
            }
            val attempts = playthroughs.guesses(locked.id).count { it.position == locked.currentRound }
            val revealed = playthroughs.reveals(locked.id).count { it.position == locked.currentRound }
            reveals.check(RoundState(locked.currentRound, RoundStatus.ACTIVE), attempts, revealed)
            playthroughs.insertReveal(locked.id, locked.currentRound, clueId, attempts, clock.instant())
            locked.id
        }!!
        return view(id)
    }

    suspend fun guess(playerId: UUID, tmdbId: Int): Game {
        val playthrough = activePlaythrough(playerId)
        // Looked up before the transaction so no connection is held during the TMDB call. This also
        // checks that the id is a real movie and gives the title shown back to the player.
        val guessed = tmdb.find(tmdbId)
            ?: throw ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_FAILED", "Unknown movie", "TMDB has no movie $tmdbId")

        val id = tx.executeAndAwait {
            val locked = playthroughs.lock(playthrough.id)
            ensureInProgress(locked)
            val game = games.findById(locked.gameId)!!
            val position = locked.currentRound
            val inRound = playthroughs.guesses(locked.id).filter { it.position == position }
            if (inRound.any { it.tmdbId == tmdbId }) {
                throw ApiException(HttpStatus.CONFLICT, "ALREADY_GUESSED", "Already guessed", "Movie $tmdbId was already guessed in this round")
            }

            val correct = tmdbId == game.movies[position].id
            val attemptNo = inRound.size + 1
            val now = clock.instant()
            playthroughs.insertGuess(
                locked.id, GuessRecord(position, attemptNo, tmdbId, guessed.title, guessed.releaseYear, correct), now,
            )

            val outcome = when {
                correct -> RoundStatus.SOLVED
                attemptNo >= game.attemptsPerRound -> RoundStatus.FAILED
                else -> RoundStatus.ACTIVE
            }
            if (outcome.ended) {
                playthroughs.setRoundStatus(locked.id, position, outcome)
                if (position + 1 < game.roundsPerGame) {
                    playthroughs.setRoundStatus(locked.id, position + 1, RoundStatus.ACTIVE)
                    playthroughs.setProgress(locked.id, PlaythroughStatus.IN_PROGRESS, position + 1, null)
                } else {
                    playthroughs.setProgress(locked.id, PlaythroughStatus.COMPLETE, position, now)
                }
            }
            locked.id
        }!!
        return view(id)
    }

    private suspend fun activePlaythrough(playerId: UUID): Playthrough {
        val playthrough = playthroughs.findForDate(playerId, today()) ?: throw gameNotStarted()
        ensureInProgress(playthrough)
        return playthrough
    }

    private fun ensureInProgress(playthrough: Playthrough) {
        if (playthrough.status == PlaythroughStatus.COMPLETE) {
            throw ApiException(HttpStatus.CONFLICT, "GAME_COMPLETE", "Game is complete", "Today's game is already finished")
        }
    }

    private fun gameNotStarted() =
        ApiException(HttpStatus.NOT_FOUND, "GAME_NOT_STARTED", "Game not started", "Start today's game first")

    private suspend fun view(playthroughId: UUID): Game {
        val playthrough = playthroughs.find(playthroughId)!!
        val state = PlaythroughState(
            playthrough = playthrough,
            game = games.findById(playthrough.gameId)!!,
            rounds = playthroughs.rounds(playthroughId),
            guesses = playthroughs.guesses(playthroughId),
            reveals = playthroughs.reveals(playthroughId),
        )
        return GameViewMapper.toApi(state)
    }
}
