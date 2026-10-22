package xyz.engsmyre.moviedescriptiongame.player

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import org.springframework.http.HttpStatus
import java.util.UUID

internal const val PLAYER_ID_KEY = "playerId"

/** The authenticated player of the current request (set by [PlayerAuthFilter]). */
suspend fun currentPlayerId(): UUID =
    currentCoroutineContext()[ReactorContext]?.context?.getOrEmpty<UUID>(PLAYER_ID_KEY)?.orElse(null)
        ?: throw ApiException(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "Missing or invalid token")
