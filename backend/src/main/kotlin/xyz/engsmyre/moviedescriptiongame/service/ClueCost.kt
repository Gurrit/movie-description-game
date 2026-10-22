package xyz.engsmyre.moviedescriptiongame.service

import xyz.engsmyre.moviedescriptiongame.api.model.ClueType

/** Points deducted from a movie's score when this clue is revealed. */
fun ClueType.pointCost(): Int = when (this) {
    ClueType.YEAR -> 50
    ClueType.DIRECTOR -> 100
    ClueType.ACTOR_1 -> 150
    ClueType.ACTOR_2 -> 200
    ClueType.ACTOR_3 -> 250
    ClueType.GENRE -> 75
    ClueType.PLOT_HINT -> 300
    ClueType.TITLE_HINT -> 500
}
