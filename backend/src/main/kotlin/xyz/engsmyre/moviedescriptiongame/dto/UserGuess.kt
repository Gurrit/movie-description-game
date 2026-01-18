package xyz.engsmyre.moviedescriptiongame.dto

import org.jetbrains.annotations.NotNull

data class UserGuess(@NotNull val movieGuess : String, @NotNull val gameId : String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserGuess

        if (movieGuess != other.movieGuess) return false
        if (gameId != other.gameId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = movieGuess.hashCode()
        result = 31 * result + gameId.hashCode()
        return result
    }

    override fun toString(): String {
        return "UserGuess(movieGuess='$movieGuess', gameId=$gameId)"
    }
}