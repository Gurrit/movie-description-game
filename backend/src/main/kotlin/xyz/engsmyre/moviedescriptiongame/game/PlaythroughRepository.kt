package xyz.engsmyre.moviedescriptiongame.game

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Repository
import xyz.engsmyre.moviedescriptiongame.common.bindNullable
import xyz.engsmyre.moviedescriptiongame.common.bool
import xyz.engsmyre.moviedescriptiongame.common.int
import xyz.engsmyre.moviedescriptiongame.common.intOrNull
import xyz.engsmyre.moviedescriptiongame.common.string
import io.r2dbc.spi.Readable
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Repository
class PlaythroughRepository(private val db: DatabaseClient) {

    private fun Readable.toPlaythrough() = Playthrough(
        id = get("id", UUID::class.java)!!,
        playerId = get("player_id", UUID::class.java)!!,
        gameId = get("game_id", UUID::class.java)!!,
        status = PlaythroughStatus.valueOf(string("status")),
        currentRound = int("current_round"),
    )

    /** The player's playthrough of the game dated [date] (the same game choice as GameRepository.findByDate). */
    suspend fun findForDate(playerId: UUID, date: LocalDate): Playthrough? =
        db.sql(
            """SELECT * FROM playthrough WHERE player_id = :player AND game_id =
               (SELECT game_id FROM daily_game WHERE date = :date ORDER BY game_id LIMIT 1)"""
        ).bind("player", playerId).bind("date", date).map { r -> r.toPlaythrough() }.awaitOneOrNull()

    suspend fun find(id: UUID): Playthrough? =
        db.sql("SELECT * FROM playthrough WHERE id = :id").bind("id", id).map { r -> r.toPlaythrough() }.awaitOneOrNull()

    suspend fun findByPlayerAndGame(playerId: UUID, gameId: UUID): Playthrough? =
        db.sql("SELECT * FROM playthrough WHERE player_id = :player AND game_id = :game")
            .bind("player", playerId).bind("game", gameId).map { r -> r.toPlaythrough() }.awaitOneOrNull()

    /** Locks the row until the end of the transaction, serialising everything one player does in a game. */
    suspend fun lock(id: UUID): Playthrough =
        db.sql("SELECT * FROM playthrough WHERE id = :id FOR UPDATE").bind("id", id)
            .map { r -> r.toPlaythrough() }.awaitOneOrNull() ?: error("playthrough $id vanished")

    /** Returns false if the player already has a playthrough of this game (a concurrent start). */
    suspend fun insert(playthrough: Playthrough, rounds: Int, now: Instant): Boolean {
        val inserted = db.sql(
            """INSERT INTO playthrough (id, player_id, game_id, status, current_round, started_at)
               VALUES (:id, :player, :game, :status, :current, :now) ON CONFLICT (player_id, game_id) DO NOTHING"""
        ).bind("id", playthrough.id).bind("player", playthrough.playerId).bind("game", playthrough.gameId)
            .bind("status", playthrough.status.name).bind("current", playthrough.currentRound).bind("now", now)
            .fetch().awaitRowsUpdated()
        if (inserted == 0L) return false
        for (position in 0 until rounds) {
            val status = if (position == playthrough.currentRound) RoundStatus.ACTIVE else RoundStatus.PENDING
            db.sql("INSERT INTO playthrough_round (playthrough_id, position, status) VALUES (:id, :pos, :status)")
                .bind("id", playthrough.id).bind("pos", position).bind("status", status.name).await()
        }
        return true
    }

    suspend fun rounds(id: UUID): List<RoundState> =
        db.sql("SELECT position, status FROM playthrough_round WHERE playthrough_id = :id ORDER BY position")
            .bind("id", id).map { r -> RoundState(r.int("position"), RoundStatus.valueOf(r.string("status"))) }
            .all().collectList().awaitSingle()

    suspend fun guesses(id: UUID): List<GuessRecord> =
        db.sql(
            """SELECT position, attempt_no, tmdb_id, title, release_year, correct FROM guess
               WHERE playthrough_id = :id ORDER BY position, attempt_no"""
        ).bind("id", id).map { r ->
            GuessRecord(r.int("position"), r.int("attempt_no"), r.int("tmdb_id"), r.string("title"), r.intOrNull("release_year"), r.bool("correct"))
        }.all().collectList().awaitSingle()

    suspend fun reveals(id: UUID): List<RevealRecord> =
        db.sql("SELECT position, clue_id FROM revealed_clue WHERE playthrough_id = :id ORDER BY revealed_at")
            .bind("id", id).map { r -> RevealRecord(r.int("position"), r.string("clue_id")) }
            .all().collectList().awaitSingle()

    suspend fun insertGuess(id: UUID, guess: GuessRecord, now: Instant) {
        db.sql(
            """INSERT INTO guess (playthrough_id, position, attempt_no, tmdb_id, title, release_year, correct, created_at)
               VALUES (:id, :pos, :attempt, :tmdb, :title, :year, :correct, :now)"""
        ).bind("id", id).bind("pos", guess.position).bind("attempt", guess.attemptNo).bind("tmdb", guess.tmdbId)
            .bind("title", guess.title).bindNullable("year", guess.releaseYear, Integer::class.java)
            .bind("correct", guess.correct).bind("now", now).await()
    }

    /** Idempotent: revealing the same clue twice keeps the first reveal. */
    suspend fun insertReveal(id: UUID, position: Int, clueId: String, attemptsUsed: Int, now: Instant) {
        db.sql(
            """INSERT INTO revealed_clue (playthrough_id, position, clue_id, revealed_at, attempts_used_at_reveal)
               VALUES (:id, :pos, :clue, :now, :attempts) ON CONFLICT DO NOTHING"""
        ).bind("id", id).bind("pos", position).bind("clue", clueId).bind("now", now).bind("attempts", attemptsUsed).await()
    }

    suspend fun setRoundStatus(id: UUID, position: Int, status: RoundStatus) {
        db.sql("UPDATE playthrough_round SET status = :status WHERE playthrough_id = :id AND position = :pos")
            .bind("status", status.name).bind("id", id).bind("pos", position).await()
    }

    suspend fun setProgress(id: UUID, status: PlaythroughStatus, currentRound: Int, completedAt: Instant?) {
        db.sql("UPDATE playthrough SET status = :status, current_round = :current, completed_at = :completed WHERE id = :id")
            .bind("status", status.name).bind("current", currentRound)
            .bindNullable("completed", completedAt, Instant::class.java).bind("id", id).await()
    }
}
