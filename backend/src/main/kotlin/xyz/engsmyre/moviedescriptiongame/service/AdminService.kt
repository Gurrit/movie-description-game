package xyz.engsmyre.moviedescriptiongame.service

import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import xyz.engsmyre.moviedescriptiongame.tmdb.mapper.TmdbMapper
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.PopularMoviesTmdbClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class AdminService @Autowired constructor(
    private val popularMoviesTmdbClient: PopularMoviesTmdbClient,
    private val challengeRepository: ChallengeRepository,
    private val tmdbMapper: TmdbMapper,
    private val challengeCount: Int = 5
) {

    fun updateCurrentChallenge(): ChallengeEntity {
        val tmdbMovies = popularMoviesTmdbClient.getPopularMoviesFromPage(1)
        val selectedMovies = tmdbMovies.shuffled().take(challengeCount)

        val movies = selectedMovies.map { movie ->
            tmdbMapper.toMovieSubEntity(movie)
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