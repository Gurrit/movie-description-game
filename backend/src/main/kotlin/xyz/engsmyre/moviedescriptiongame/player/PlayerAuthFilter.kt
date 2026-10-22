package xyz.engsmyre.moviedescriptiongame.player

import kotlinx.coroutines.reactor.mono
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import xyz.engsmyre.moviedescriptiongame.common.ProblemResponses
import java.time.Clock
import java.time.Duration
import java.util.Optional
import java.util.UUID

/** Resolves the bearer token of every API call (except the public ones) to a player. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class PlayerAuthFilter(
    private val players: PlayerRepository,
    private val tokens: TokenService,
    private val problems: ProblemResponses,
    private val clock: Clock,
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!requiresAuth(exchange)) return chain.filter(exchange)

        val header = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val token = header?.takeIf { it.startsWith(BEARER, ignoreCase = true) }?.substring(BEARER.length)?.trim()
        if (token.isNullOrEmpty()) return problems.write(exchange, 401, "INVALID_TOKEN", "Missing or invalid token")

        return mono { Optional.ofNullable(authenticate(token)) }
            .flatMap { found ->
                if (found.isPresent) {
                    chain.filter(exchange).contextWrite(Context.of(PLAYER_ID_KEY, found.get()))
                } else {
                    problems.write(exchange, 401, "INVALID_TOKEN", "Missing or invalid token")
                }
            }
    }

    private suspend fun authenticate(token: String): UUID? {
        val id = players.findIdByTokenHash(tokens.hash(token)) ?: return null
        val now = clock.instant()
        players.touch(id, now, now.minus(TOUCH_INTERVAL))
        return id
    }

    private fun requiresAuth(exchange: ServerWebExchange): Boolean {
        val path = exchange.request.uri.path
        if (!path.startsWith("/api/")) return false
        if (path.startsWith("/api/internal/") || path.startsWith("/api/healthz")) return false
        if (path == "/api/players" && exchange.request.method == HttpMethod.POST) return false
        return true
    }

    private companion object {
        const val BEARER = "Bearer "
        val TOUCH_INTERVAL: Duration = Duration.ofHours(1)
    }
}
