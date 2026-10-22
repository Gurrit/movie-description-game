package xyz.engsmyre.moviedescriptiongame.stats

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOne
import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.api.model.Stats
import xyz.engsmyre.moviedescriptiongame.common.int
import java.util.UUID

@Service
class StatsService(private val db: DatabaseClient) {

    suspend fun statsFor(playerId: UUID): Stats = db.sql(
        """SELECT
             (SELECT count(*) FROM playthrough p
               WHERE p.player_id = :player AND p.status = 'COMPLETE') AS games_played,
             (SELECT count(*) FROM playthrough p
               WHERE p.player_id = :player AND p.status = 'COMPLETE'
                 AND NOT EXISTS (SELECT 1 FROM playthrough_round r
                                  WHERE r.playthrough_id = p.id AND r.status <> 'SOLVED')) AS fully_solved,
             (SELECT count(*) FROM playthrough_round r
                JOIN playthrough p ON p.id = r.playthrough_id
               WHERE p.player_id = :player AND r.status = 'SOLVED') AS movies_solved"""
    ).bind("player", playerId)
        .map { r -> Stats(gamesPlayed = r.int("games_played"), gamesFullySolved = r.int("fully_solved"), moviesSolved = r.int("movies_solved")) }
        .awaitOne()
}
