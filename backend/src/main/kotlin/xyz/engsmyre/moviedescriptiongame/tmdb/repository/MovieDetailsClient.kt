package xyz.engsmyre.moviedescriptiongame.tmdb.repository

import xyz.engsmyre.moviedescriptiongame.tmdb.domain.TmdbCreditsResponse
import xyz.engsmyre.moviedescriptiongame.tmdb.domain.TmdbMovieDetailsResponse

interface MovieDetailsClient {
    fun getMovieDetails(movieId: Int): TmdbMovieDetailsResponse
    fun getCredits(movieId: Int): TmdbCreditsResponse
}
