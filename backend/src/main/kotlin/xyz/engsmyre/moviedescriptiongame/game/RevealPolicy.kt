package xyz.engsmyre.moviedescriptiongame.game

import org.springframework.stereotype.Component

/**
 * Decides whether a clue may be revealed right now. The single place to change the game's clue
 * rules, e.g. "a reveal costs an attempt" or "one clue is unlocked per wrong guess".
 */
interface RevealPolicy {
    /** Throws an ApiException if the reveal is not allowed. */
    fun check(round: RoundState, attemptsUsed: Int, revealedSoFar: Int)
}

/** Current rules: any clue, at any time, at no cost. */
@Component
class FreeRevealPolicy : RevealPolicy {
    override fun check(round: RoundState, attemptsUsed: Int, revealedSoFar: Int) = Unit
}
