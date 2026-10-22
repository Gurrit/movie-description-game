package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ratelimit")
data class RateLimitProperties(
    val playersPerMinute: Int = 20,
    val searchPerMinute: Int = 90,
)
