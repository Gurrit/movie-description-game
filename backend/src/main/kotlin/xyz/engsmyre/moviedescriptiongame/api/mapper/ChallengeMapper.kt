package xyz.engsmyre.moviedescriptiongame.api.mapper

import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.api.dto.ChallengeResponse
import xyz.engsmyre.moviedescriptiongame.api.dto.MovieInfo
import xyz.engsmyre.moviedescriptiongame.db.entity.ChallengeEntity
import xyz.engsmyre.moviedescriptiongame.db.entity.MovieSubEntity
import java.time.LocalDate

@Component
class ChallengeMapper {

    fun toResponse(challenge: ChallengeEntity): ChallengeResponse {
        return ChallengeResponse(
            id = challenge.id.toHexString(),
            date = LocalDate.parse(challenge.day),
            movies = challenge.movies.map { toMovieInfo(it) }
        )
    }

    private fun toMovieInfo(movie: MovieSubEntity): MovieInfo {
        return MovieInfo(
            id = movie.tmdbId,
            title = movie.movieTitle,
            originalTitle = movie.movieOriginalTitle,
            year = movie.year,
            directors = movie.directors,
            genres = movie.genres,
            posterUrl = movie.coverArtUrl,
            description = movie.description
        )
    }
}
