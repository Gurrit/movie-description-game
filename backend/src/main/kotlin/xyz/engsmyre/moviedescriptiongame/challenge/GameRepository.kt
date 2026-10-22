package xyz.engsmyre.moviedescriptiongame.challenge

import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.await
import org.springframework.stereotype.Repository
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue
import xyz.engsmyre.moviedescriptiongame.clue.Clue
import xyz.engsmyre.moviedescriptiongame.common.bindNullable
import xyz.engsmyre.moviedescriptiongame.common.int
import xyz.engsmyre.moviedescriptiongame.common.intOrNull
import xyz.engsmyre.moviedescriptiongame.common.string
import xyz.engsmyre.moviedescriptiongame.common.stringOrNull
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Repository
class GameRepository(private val db: DatabaseClient, private val json: JsonMapper) {

    /** The game for [date]. If there were ever several, the lowest id wins so everybody gets the same one. */
    suspend fun findByDate(date: LocalDate): DailyGame? {
        val row = db.sql(
            """SELECT g.id, g.rounds_per_game, g.attempts_per_round FROM daily_game dg
               JOIN game g ON g.id = dg.game_id WHERE dg.date = :date ORDER BY g.id LIMIT 1"""
        ).bind("date", date)
            .map { r -> Triple(r.get("id", UUID::class.java)!!, r.int("rounds_per_game"), r.int("attempts_per_round")) }
            .awaitOneOrNull() ?: return null
        return DailyGame(row.first, date, row.second, row.third, findMovies(row.first))
    }

    suspend fun findById(id: UUID): DailyGame? {
        val row = db.sql(
            """SELECT g.rounds_per_game, g.attempts_per_round, dg.date FROM game g
               JOIN daily_game dg ON dg.game_id = g.id WHERE g.id = :id"""
        ).bind("id", id)
            .map { r -> Triple(r.int("rounds_per_game"), r.int("attempts_per_round"), r.get("date", LocalDate::class.java)!!) }
            .awaitOneOrNull() ?: return null
        return DailyGame(id, row.third, row.first, row.second, findMovies(id))
    }

    private suspend fun findMovies(gameId: UUID): List<Movie> =
        db.sql(
            """SELECT m.id, m.title, m.original_title, m.release_year, m.poster_path, m.clues FROM game_movie gm
               JOIN movie m ON m.id = gm.movie_id WHERE gm.game_id = :gameId ORDER BY gm.position"""
        ).bind("gameId", gameId)
            .map { r ->
                Movie(
                    id = r.int("id"),
                    title = r.string("title"),
                    originalTitle = r.string("original_title"),
                    releaseYear = r.int("release_year"),
                    posterPath = r.stringOrNull("poster_path"),
                    clues = json.readValue<List<Clue>>(r.get("clues", Json::class.java)!!.asString()),
                )
            }
            .all().collectList().awaitSingle()

    /** Ids of movies used in games dated on or after [from]; they may not be reused yet. */
    suspend fun movieIdsUsedSince(from: LocalDate): Set<Int> =
        db.sql(
            """SELECT DISTINCT gm.movie_id FROM game_movie gm
               JOIN daily_game dg ON dg.game_id = gm.game_id WHERE dg.date >= :from"""
        ).bind("from", from).map { r -> r.int("movie_id") }
            .all().collectList().awaitSingle().toSet()

    /** Serialises generation per date for the rest of the current transaction. */
    suspend fun lockDate(date: LocalDate) {
        db.sql("SELECT 1 AS locked FROM (SELECT pg_advisory_xact_lock(:key)) AS l")
            .bind("key", ADVISORY_LOCK_NAMESPACE + date.toEpochDay())
            .map { _ -> 1 }.awaitOne()
    }

    /** Stores the game and its movies. Must run in a transaction. */
    suspend fun insert(game: DailyGame, now: Instant) {
        for (movie in game.movies) {
            db.sql(
                """INSERT INTO movie (id, title, original_title, release_year, poster_path, clues)
                   VALUES (:id, :title, :originalTitle, :year, :poster, :clues) ON CONFLICT (id) DO NOTHING"""
            ).bind("id", movie.id).bind("title", movie.title).bind("originalTitle", movie.originalTitle)
                .bind("year", movie.releaseYear).bindNullable("poster", movie.posterPath, String::class.java)
                .bind("clues", Json.of(json.writeValueAsString(movie.clues))).await()
        }
        db.sql(
            """INSERT INTO game (id, rounds_per_game, attempts_per_round, created_at)
               VALUES (:id, :rounds, :attempts, :now)"""
        ).bind("id", game.id).bind("rounds", game.roundsPerGame).bind("attempts", game.attemptsPerRound)
            .bind("now", now).await()
        game.movies.forEachIndexed { position, movie ->
            db.sql("INSERT INTO game_movie (game_id, position, movie_id) VALUES (:g, :p, :m)")
                .bind("g", game.id).bind("p", position).bind("m", movie.id).await()
        }
        db.sql("INSERT INTO daily_game (game_id, date) VALUES (:g, :d)")
            .bind("g", game.id).bind("d", game.date).await()
    }

    private companion object {
        /** Keeps our advisory locks apart from any other use of the same database. */
        const val ADVISORY_LOCK_NAMESPACE = 7_300_000_000L
    }
}
