package xyz.engsmyre.moviedescriptiongame.service

import org.springframework.stereotype.Service
import xyz.engsmyre.moviedescriptiongame.db.repository.ChallengeRepository
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.Movie
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.PopularMoviesTmdbClient
import kotlin.random.Random

@Service
class AdminService(
    val popularMoviesTmdbClient: PopularMoviesTmdbClient,
    val challengeRepository: ChallengeRepository,
    val challengeCount: Int = 5
) {

    fun updateCurrentChallenge(): String {
        val movies = popularMoviesTmdbClient.getPopularMoviesFromPage(1) // TODO, better algo for selecting movies
        val selectedMovies = movies.shuffled().take(challengeCount)


        return "OK" // TODO
    }

}