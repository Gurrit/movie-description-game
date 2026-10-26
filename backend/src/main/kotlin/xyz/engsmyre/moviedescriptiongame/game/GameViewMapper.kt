package xyz.engsmyre.moviedescriptiongame.game

import xyz.engsmyre.moviedescriptiongame.api.model.Answer
import xyz.engsmyre.moviedescriptiongame.api.model.Clue
import xyz.engsmyre.moviedescriptiongame.api.model.ClueContent
import xyz.engsmyre.moviedescriptiongame.api.model.ClueType
import xyz.engsmyre.moviedescriptiongame.api.model.Game
import xyz.engsmyre.moviedescriptiongame.api.model.GameStatus
import xyz.engsmyre.moviedescriptiongame.api.model.GuessResult
import xyz.engsmyre.moviedescriptiongame.api.model.Round
import xyz.engsmyre.moviedescriptiongame.clue.ClueContent as StoredContent
import xyz.engsmyre.moviedescriptiongame.api.model.RoundStatus as ApiRoundStatus

/**
 * The only place that decides what a player may see. A round starts with its first clue; every wrong
 * guess or skip unlocks the next one, and an ended round shows all of them. The answer is exposed only when the
 * round has ended; future rounds are blank.
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
        val attemptsUsed = guesses.size + round.skips
        if (round.status == RoundStatus.PENDING) {
            return Round(round.position, ApiRoundStatus.PENDING, 0, emptyList(), emptyList())
        }
        // Every attempt of a round that is still going is a wrong guess or a skip, and each unlocks one more clue
        val unlocked = attemptsUsed + 1
        return Round(
            index = round.position,
            status = ApiRoundStatus.valueOf(round.status.name),
            attemptsUsed = attemptsUsed,
            guesses = guesses.map { GuessResult(it.tmdbId, it.title, it.correct, it.releaseYear) },
            clues = movie.clues.sortedBy { it.order }.map { clue ->
                val isUnlocked = round.status.ended || clue.order <= unlocked
                Clue(
                    id = clue.id,
                    type = ClueType.valueOf(clue.type.name),
                    order = clue.order,
                    revealed = isUnlocked,
                    content = if (isUnlocked) content(clue.content) else null,
                )
            },
            answer = if (round.status.ended) Answer(movie.id, movie.title, movie.releaseYear, movie.posterPath) else null,
        )
    }

    private fun content(c: StoredContent) = ClueContent(
        text = c.text,
        genres = c.genres,
        runtimeMinutes = c.runtimeMinutes,
        // The stored clue has the exact year; a player only gets the decade
        decade = c.releaseYear?.let { it / 10 * 10 },
        names = c.names,
    )
}
