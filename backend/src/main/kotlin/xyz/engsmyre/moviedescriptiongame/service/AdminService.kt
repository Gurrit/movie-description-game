package xyz.engsmyre.moviedescriptiongame.service

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.MovieEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.PopularMoviesTmdbClient
import java.time.LocalDate

@Service
class AdminService @Autowired constructor (
    private val popularMoviesTmdbClient: PopularMoviesTmdbClient,
    private val challengeRepository: ChallengeRepository,
    private val challengeCount: Int = 5
) {

    // TODO Mapping from DTO instead of db entity
    fun updateCurrentChallenge(): ChallengeEntity {
//        val movies = popularMoviesTmdbClient.getPopularMoviesFromPage(1) // TODO, better algo for selecting movies
//        val selectedMovies = movies.shuffled().take(challengeCount)

        val challenge = challengeRepository.insertChallenge(ChallengeEntity(
            id = ObjectId.get(),
            movies = listOf(MovieEntity("The Matrix")),
            day = LocalDate.now().toString()
        ))

        return challenge
    }

    fun getChallenge(localDate: LocalDate): ChallengeEntity? {
        return challengeRepository.getChallenge(localDate)
    }

}