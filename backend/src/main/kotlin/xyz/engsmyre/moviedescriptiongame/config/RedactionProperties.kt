package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("redaction")
data class RedactionProperties(
    val maxMaskedRatio: Double = 0.35,
    val mask: String = "▇▇▇",
) {
    init {
        require(maxMaskedRatio in 0.0..1.0) { "redaction.max-masked-ratio must be in 0..1" }
    }
}
