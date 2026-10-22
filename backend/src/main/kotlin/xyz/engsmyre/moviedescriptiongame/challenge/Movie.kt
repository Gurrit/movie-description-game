package xyz.engsmyre.moviedescriptiongame.challenge

import xyz.engsmyre.moviedescriptiongame.clue.Clue
import java.time.LocalDate
import java.util.UUID

/** A movie as stored: everything needed to play it, independent of any date. [id] is the TMDB id. */
data class Movie(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val releaseYear: Int,
    val posterPath: String?,
    val clues: List<Clue>,
)

/** A game: shared by all players. [movies] are in round order. */
data class DailyGame(
    val id: UUID,
    val date: LocalDate,
    val roundsPerGame: Int,
    val attemptsPerRound: Int,
    val movies: List<Movie>,
)
