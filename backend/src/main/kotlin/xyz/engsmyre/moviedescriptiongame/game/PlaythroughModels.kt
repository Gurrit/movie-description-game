package xyz.engsmyre.moviedescriptiongame.game

import xyz.engsmyre.moviedescriptiongame.challenge.DailyGame
import java.util.UUID

enum class PlaythroughStatus { IN_PROGRESS, COMPLETE }

enum class RoundStatus {
    PENDING, ACTIVE, SOLVED, FAILED;

    val ended: Boolean get() = this == SOLVED || this == FAILED
}

data class Playthrough(
    val id: UUID,
    val playerId: UUID,
    val gameId: UUID,
    val status: PlaythroughStatus,
    val currentRound: Int,
)

data class RoundState(val position: Int, val status: RoundStatus)

data class GuessRecord(
    val position: Int,
    val attemptNo: Int,
    val tmdbId: Int,
    val title: String,
    val releaseYear: Int?,
    val correct: Boolean,
)

data class RevealRecord(val position: Int, val clueId: String)

/** Everything needed to render a playthrough. */
data class PlaythroughState(
    val playthrough: Playthrough,
    val game: DailyGame,
    val rounds: List<RoundState>,
    val guesses: List<GuessRecord>,
    val reveals: List<RevealRecord>,
)
