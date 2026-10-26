package xyz.engsmyre.moviedescriptiongame.common

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import xyz.engsmyre.moviedescriptiongame.config.RateLimitProperties
import java.time.Clock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Per-IP fixed-window limiter for the two endpoints open to abuse: creating players and TMDB search (and random picks, which cost TMDB calls too).
 * In-process, which is enough for a single instance; behind a proxy the client IP comes from X-Forwarded-For.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RateLimitFilter(
    private val props: RateLimitProperties,
    private val problems: ProblemResponses,
    private val clock: Clock,
) : WebFilter {

    private class Window(val minute: Long) {
        val count = AtomicInteger()
    }

    private val windows = ConcurrentHashMap<String, Window>()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val (bucket, limit) = bucketOf(exchange) ?: return chain.filter(exchange)
        val ip = exchange.request.remoteAddress?.address?.hostAddress ?: "unknown"
        val minute = clock.millis() / 60_000
        val window = windows.compute("$bucket|$ip") { _, w -> if (w == null || w.minute != minute) Window(minute) else w }!!
        if (window.count.incrementAndGet() > limit) {
            return problems.write(exchange, 429, "RATE_LIMITED", "Too many requests")
        }
        if (windows.size > MAX_TRACKED) windows.values.removeIf { it.minute < minute }
        return chain.filter(exchange)
    }

    private fun bucketOf(exchange: ServerWebExchange): Pair<String, Int>? {
        val path = exchange.request.uri.path
        return when {
            path == "/api/players" && exchange.request.method == HttpMethod.POST -> "players" to props.playersPerMinute
            path == "/api/movies/search" || path == "/api/movies/random" -> "search" to props.searchPerMinute
            else -> null
        }
    }

    private companion object {
        const val MAX_TRACKED = 10_000
    }
}
