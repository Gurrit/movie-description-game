package xyz.engsmyre.moviedescriptiongame.api.dto

enum class ClueType(val pointCost: Int) {
    YEAR(50),
    DIRECTOR(100),
    ACTOR_1(150),
    ACTOR_2(200),
    ACTOR_3(250),
    GENRE(75),
    PLOT_HINT(300),
    TITLE_HINT(500)
}
