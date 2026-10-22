package xyz.engsmyre.moviedescriptiongame.player

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import xyz.engsmyre.moviedescriptiongame.api.generated.PlayerApi
import xyz.engsmyre.moviedescriptiongame.api.model.Player
import xyz.engsmyre.moviedescriptiongame.api.model.Stats
import xyz.engsmyre.moviedescriptiongame.stats.StatsService
import java.time.Clock
import java.util.UUID

@RestController
class PlayerController(
    private val players: PlayerRepository,
    private val tokens: TokenService,
    private val stats: StatsService,
    private val clock: Clock,
) : PlayerApi {

    override suspend fun createPlayer(): ResponseEntity<Player> {
        val id = UUID.randomUUID()
        val token = tokens.newToken()
        players.insert(id, tokens.hash(token), clock.instant())
        return ResponseEntity.status(HttpStatus.CREATED).body(Player(playerId = id, token = token))
    }

    override suspend fun getMyStats(): ResponseEntity<Stats> = ResponseEntity.ok(stats.statsFor(currentPlayerId()))
}
