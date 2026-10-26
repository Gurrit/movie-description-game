package xyz.engsmyre.moviedescriptiongame.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("game")
data class GameProperties(
    val roundsPerGame: Int = 7,
    val attemptsPerRound: Int = 7,
) {
    init {
        require(roundsPerGame >= 1) { "game.rounds-per-game must be >= 1" }
        require(attemptsPerRound >= 1) { "game.attempts-per-round must be >= 1" }
    }
}
