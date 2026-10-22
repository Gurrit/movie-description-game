package xyz.engsmyre.moviedescriptiongame.player

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class PlayerRepository(private val db: DatabaseClient) {

    suspend fun insert(id: UUID, tokenHash: ByteArray, now: Instant) {
        db.sql("INSERT INTO player (id, token_hash, created_at, last_seen_at) VALUES (:id, :hash, :now, :now)")
            .bind("id", id).bind("hash", tokenHash).bind("now", now).await()
    }

    suspend fun findIdByTokenHash(hash: ByteArray): UUID? =
        db.sql("SELECT id FROM player WHERE token_hash = :hash").bind("hash", hash)
            .map { r -> r.get("id", UUID::class.java)!! }.awaitOneOrNull()

    /** Updates last_seen_at only if it is older than [staleBefore], to avoid a write on every request. */
    suspend fun touch(id: UUID, now: Instant, staleBefore: Instant) {
        db.sql("UPDATE player SET last_seen_at = :now WHERE id = :id AND last_seen_at < :stale")
            .bind("id", id).bind("now", now).bind("stale", staleBefore).await()
    }
}
