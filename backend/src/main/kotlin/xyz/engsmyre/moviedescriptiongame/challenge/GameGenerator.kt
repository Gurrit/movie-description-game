package xyz.engsmyre.moviedescriptiongame.challenge

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import xyz.engsmyre.moviedescriptiongame.common.ApiException
import xyz.engsmyre.moviedescriptiongame.config.GameProperties
import xyz.engsmyre.moviedescriptiongame.config.GenerationProperties
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class GameGenerator(
    private val repository: GameRepository,
    private val selector: MovieSelector,
    private val tx: TransactionalOperator,
    private val game: GameProperties,
    private val generation: GenerationProperties,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /** Returns the game for [date], generating it first if it does not exist. */
    suspend fun ensure(date: LocalDate): DailyGame {
        repository.findByDate(date)?.let { return it }

        // TMDB is slow, so no transaction (and no DB connection) is held while selecting. If two
        // instances race, both select but only the first insert wins; the second finds the winner's game.
        val excluded = repository.movieIdsUsedSince(date.minusDays(generation.noRepeatDays.toLong()))
        val movies = try {
            selector.select(game.roundsPerGame, excluded)
        } catch (e: NotEnoughMoviesException) {
            log.error("Generation for {} failed: {}", date, e.message)
            throw ApiException(
                HttpStatus.SERVICE_UNAVAILABLE, "GENERATION_FAILED", "Could not generate today's game", e.message,
            )
        }
        val candidate = DailyGame(UUID.randomUUID(), date, game.roundsPerGame, game.attemptsPerRound, movies)

        return tx.executeAndAwait {
            repository.lockDate(date)
            repository.findByDate(date)?.also { log.info("Game for {} was generated concurrently", date) }
                ?: candidate.also {
                    repository.insert(it, clock.instant())
                    log.info("Generated game {} for {}: {}", it.id, date, it.movies.map { m -> m.title })
                }
        }!!
    }
}
