package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("generation")
data class GenerationProperties(
    val cron: String = "0 0 0 * * *",
    val maxAttempts: Int = 100,
    val preGenerateDays: Int = 0,
    val internalToken: String = "",
) {
    init {
        require(maxAttempts >= 1) { "generation.max-attempts must be >= 1" }
        require(preGenerateDays >= 0) { "generation.pre-generate-days must be >= 0" }
    }
}
