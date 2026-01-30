package xyz.engsmyre.moviedescriptiongame.service

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.MovieSubEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.PopularMoviesTmdbClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class AdminService @Autowired constructor (
    private val popularMoviesTmdbClient: PopularMoviesTmdbClient,
    private val challengeRepository: ChallengeRepository,
    private val challengeCount: Int = 5
) {

    // TODO Mapping from DTO instead of db entity
    fun updateCurrentChallenge(): ChallengeEntity {
        val tmdbMovies = popularMoviesTmdbClient.getPopularMoviesFromPage(1) // TODO, better algo for selecting movies
        val selectedMovies = tmdbMovies.shuffled().take(challengeCount)

        // TODO Move to mapper class instead and add validation.
        val movies = selectedMovies.map { movie ->
            MovieSubEntity(
                movieTitle = movie.primaryTitle!!,
                movieOriginalTitle = movie.originalTitle!!,
                year = movie.releaseDate!!.year.toString(),
                directors = listOf("T"),
                billings = mapOf(1 to "TODO"),
                writer = "TODO",
                budget = -1,
                coverArtUrl = "http://example.com",
            )
        }
        val challenge = challengeRepository.insertChallenge(ChallengeEntity(
            id = ObjectId.get(),
            movies = movies,
            day = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            ))

        return challenge
    }

    fun getChallenge(localDate: LocalDate): ChallengeEntity? {
        return challengeRepository.getChallenge(localDate)
    }

}