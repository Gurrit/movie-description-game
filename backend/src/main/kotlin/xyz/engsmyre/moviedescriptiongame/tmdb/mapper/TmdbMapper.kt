package xyz.engsmyre.moviedescriptiongame.tmdb.mapper

import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.db.entity.MovieSubEntity
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.Movie
import xyz.engsmyre.moviedescriptiongame.tmdb.repository.MovieDetailsClient

@Component
class TmdbMapper(
    private val movieDetailsClient: MovieDetailsClient
) {

    fun toMovieSubEntity(tmdbMovie: Movie): MovieSubEntity {
        val details = movieDetailsClient.getMovieDetails(tmdbMovie.id)
        val credits = movieDetailsClient.getCredits(tmdbMovie.id)

        val posterUrl = details.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            ?: tmdbMovie.coverArtPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            ?: ""

        val directors = credits.crew
            ?.filter { it.job.equals("Director", ignoreCase = true) }
            ?.map { it.name }
            ?: emptyList()

        val topActors = credits.cast
            ?.sortedBy { it.order }
            ?.take(3)
            ?.map { it.name }
            ?: emptyList()

        val writers = credits.crew
            ?.filter { 
                it.job.equals("Screenplay", ignoreCase = true) ||
                it.job.equals("Writer", ignoreCase = true) ||
                it.job.equals("Story", ignoreCase = true)
            }
            ?.map { it.name }
            ?.distinct()
            ?: emptyList()

        val genres = details.genres?.map { it.name } ?: emptyList()
        val billings = topActors.mapIndexed { index, actor -> (index + 1) to actor }.toMap()
        val description = details.overview ?: tmdbMovie.description

        return MovieSubEntity(
            tmdbId = tmdbMovie.id,
            movieTitle = tmdbMovie.primaryTitle ?: details.title ?: "Unknown",
            movieOriginalTitle = tmdbMovie.originalTitle ?: details.originalTitle ?: "Unknown",
            year = tmdbMovie.releaseDate?.year?.toString() ?: details.releaseDate?.year?.toString() ?: "Unknown",
            directors = directors,
            billings = billings,
            writer = writers.firstOrNull() ?: "Unknown",
            budget = details.budget,
            coverArtUrl = posterUrl,
            genres = genres,
            description = description
        )
    }
}
