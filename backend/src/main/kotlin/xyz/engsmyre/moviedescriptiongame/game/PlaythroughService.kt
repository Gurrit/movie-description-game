package xyz.engsmyre.moviedescriptiongame.game

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import xyz.engsmyre.moviedescriptiongame.api.model.Game
import xyz.engsmyre.moviedescriptiongame.challenge.DailyGame
import xyz.engsmyre.moviedescriptiongame.challenge.GameGenerator
import xyz.engsmyre.moviedescriptiongame.challenge.GameRepository
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbClient
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class StartedGame(val game: Game, val created: Boolean)

/** The player's side of a game: starting it, guessing and skipping. */
@Service
class PlaythroughService(
    private val playthroughs: PlaythroughRepository,
    private val games: GameRepository,
    private val generator: GameGenerator,
    private val tmdb: TmdbClient,
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
            val attemptNo = inRound.size + playthroughs.skips(locked.id, position) + 1
            val now = clock.instant()
            playthroughs.insertGuess(
                locked.id, GuessRecord(position, attemptNo, tmdbId, guessed.title, guessed.releaseYear, correct), now,
            )
            settle(locked.id, game, position, attemptNo, correct, now)
            locked.id
        }!!
        return view(id)
    }

    /** Gives up an attempt without guessing. Like a wrong guess it unlocks the next clue, and it can fail the round. */
    suspend fun skip(playerId: UUID): Game {
        val playthrough = activePlaythrough(playerId)
        val id = tx.executeAndAwait {
            val locked = playthroughs.lock(playthrough.id)
            ensureInProgress(locked)
            val game = games.findById(locked.gameId)!!
            val position = locked.currentRound
            val guesses = playthroughs.guesses(locked.id).count { it.position == position }
            val attemptNo = guesses + playthroughs.skips(locked.id, position) + 1
            playthroughs.addSkip(locked.id, position)
            settle(locked.id, game, position, attemptNo, correct = false, now = clock.instant())
            locked.id
        }!!
        return view(id)
    }

    /** Ends the round if the attempt solved it or was the last one, and moves the game on. */
    private suspend fun settle(id: UUID, game: DailyGame, position: Int, attemptNo: Int, correct: Boolean, now: Instant) {
        val outcome = when {
            correct -> RoundStatus.SOLVED
            attemptNo >= game.attemptsPerRound -> RoundStatus.FAILED
            else -> RoundStatus.ACTIVE
        }
        if (!outcome.ended) return
        playthroughs.setRoundStatus(id, position, outcome)
        if (position + 1 < game.roundsPerGame) {
            playthroughs.setRoundStatus(id, position + 1, RoundStatus.ACTIVE)
            playthroughs.setProgress(id, PlaythroughStatus.IN_PROGRESS, position + 1, null)
        } else {
            playthroughs.setProgress(id, PlaythroughStatus.COMPLETE, position, now)
        }
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
        )
        return GameViewMapper.toApi(state)
    }
}
