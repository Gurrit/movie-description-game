package xyz.engsmyre.moviedescriptiongame.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import xyz.engsmyre.moviedescriptiongame.config.RateLimitProperties
import java.net.InetSocketAddress
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class RateLimitFilterTest {
    private class MutableClock(var now: Instant) : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = now
    }

    private val clock = MutableClock(Instant.parse("2026-10-22T12:00:10Z"))
    private val filter = RateLimitFilter(
        RateLimitProperties(playersPerMinute = 2, searchPerMinute = 3), ProblemResponses(JsonMapper.builder().build()), clock,
    )
    private var passed = 0
    private val chain = WebFilterChain { passed++; Mono.empty() }

    private fun call(method: HttpMethod, path: String, ip: String = "10.0.0.1"): Int {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.method(method, path).remoteAddress(InetSocketAddress(ip, 5000))
        )
        filter.filter(exchange, chain).block()
        return exchange.response.statusCode?.value() ?: 200
    }

    @Test
    fun `limits player creation per ip and minute`() {
        assertEquals(listOf(200, 200, 429), (1..3).map { call(HttpMethod.POST, "/api/players") })
        assertEquals(2, passed)
        assertEquals(200, call(HttpMethod.POST, "/api/players", ip = "10.0.0.2"), "other ips are unaffected")
    }

    @Test
    fun `the window resets after a minute`() {
        repeat(3) { call(HttpMethod.POST, "/api/players") }
        clock.now = clock.now.plusSeconds(60)
        assertEquals(200, call(HttpMethod.POST, "/api/players"))
    }

    @Test
    fun `search has its own limit and other endpoints are not limited`() {
        assertEquals(listOf(200, 200, 200, 429), (1..4).map { call(HttpMethod.GET, "/api/movies/search") })
        repeat(20) { assertEquals(200, call(HttpMethod.GET, "/api/game/today")) }
        assertEquals(200, call(HttpMethod.GET, "/api/players"), "only POST /players is limited")
    }
}
