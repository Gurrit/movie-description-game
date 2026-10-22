package xyz.engsmyre.moviedescriptiongame.game

import xyz.engsmyre.moviedescriptiongame.api.model.Answer
import xyz.engsmyre.moviedescriptiongame.api.model.Clue
import xyz.engsmyre.moviedescriptiongame.api.model.ClueContent
import xyz.engsmyre.moviedescriptiongame.api.model.ClueType
import xyz.engsmyre.moviedescriptiongame.api.model.Game
import xyz.engsmyre.moviedescriptiongame.api.model.GameStatus
import xyz.engsmyre.moviedescriptiongame.api.model.GuessResult
import xyz.engsmyre.moviedescriptiongame.api.model.PersonClue
import xyz.engsmyre.moviedescriptiongame.api.model.Round
import xyz.engsmyre.moviedescriptiongame.clue.ClueContent as StoredContent
import xyz.engsmyre.moviedescriptiongame.api.model.RoundStatus as ApiRoundStatus

/**
 * The only place that decides what a player may see. Content of a clue is exposed only when the player
 * revealed it or the round has ended; the answer only when the round has ended; future rounds are blank.
 */
object GameViewMapper {

    fun toApi(state: PlaythroughState): Game {
        val pt = state.playthrough
        val complete = pt.status == PlaythroughStatus.COMPLETE
        return Game(
            id = pt.id,
            date = state.game.date,
            status = GameStatus.valueOf(pt.status.name),
            roundsPerGame = state.game.roundsPerGame,
            attemptsPerRound = state.game.attemptsPerRound,
            currentRound = if (complete) null else pt.currentRound,
            rounds = state.rounds.map { round(state, it) },
        )
    }

    private fun round(state: PlaythroughState, round: RoundState): Round {
        val movie = state.game.movies[round.position]
        val guesses = state.guesses.filter { it.position == round.position }
        if (round.status == RoundStatus.PENDING) {
            return Round(round.position, ApiRoundStatus.PENDING, 0, emptyList(), emptyList())
        }
        val revealed = state.reveals.filter { it.position == round.position }.map { it.clueId }.toSet()
        return Round(
            index = round.position,
            status = ApiRoundStatus.valueOf(round.status.name),
            attemptsUsed = guesses.size,
            guesses = guesses.map { GuessResult(it.tmdbId, it.title, it.correct, it.releaseYear) },
            clues = movie.clues.sortedBy { it.order }.map { clue ->
                val isRevealed = clue.id in revealed
                Clue(
                    id = clue.id,
                    type = ClueType.valueOf(clue.type.name),
                    order = clue.order,
                    revealed = isRevealed,
                    content = if (isRevealed || round.status.ended) content(clue.content) else null,
                )
            },
            answer = if (round.status.ended) Answer(movie.id, movie.title, movie.releaseYear, movie.posterPath) else null,
        )
    }

    private fun content(c: StoredContent) = ClueContent(
        text = c.text,
        genres = c.genres,
        runtimeMinutes = c.runtimeMinutes,
        releaseYear = c.releaseYear,
        people = c.people?.map { PersonClue(PersonClue.Role.valueOf(it.role.name), it.name, it.character) },
    )
}
