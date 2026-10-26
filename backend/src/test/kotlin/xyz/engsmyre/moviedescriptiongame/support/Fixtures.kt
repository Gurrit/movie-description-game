package xyz.engsmyre.moviedescriptiongame.support

import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbCastMember
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbCrewMember
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbMovieDetails

object Fixtures {
    /** A fully usable movie. Ids from 1 upwards give distinct, unrelated-looking titles. */
    fun details(
        id: Int,
        title: String = "Movie $id Title",
        overview: String? = "A story about a man who wanders far and finds a home somewhere new.",
        tagline: String? = "Every journey ends somewhere",
        cast: List<TmdbCastMember> = listOf(
            TmdbCastMember("Alice Actor$id", "Hero", 0),
            TmdbCastMember("Bob Actor$id", "Sidekick", 1),
            TmdbCastMember("Carol Actor$id", "Villain", 2),
            TmdbCastMember("Dan Extra$id", "Bystander", 3),
        ),
        studios: List<String> = listOf("Studio $id Pictures", "Second Company"),
        crew: List<TmdbCrewMember> = listOf(
            TmdbCrewMember("Dora Director$id", "Director"),
            TmdbCrewMember("Wally Writer$id", "Screenplay"),
        ),
    ) = TmdbMovieDetails(
        id = id,
        title = title,
        originalTitle = title,
        overview = overview,
        tagline = tagline,
        releaseYear = 1990 + id % 30,
        runtimeMinutes = 100 + id % 40,
        posterPath = "/poster$id.jpg",
        genres = listOf("Drama", "Adventure"),
        studios = studios,
        cast = cast,
        crew = crew,
    )

    /** The TMDB JSON for [details], as served for /movie/{id}?append_to_response=credits. */
    fun detailsJson(d: TmdbMovieDetails): String {
        fun q(s: String?) = if (s == null) "null" else "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        val cast = d.cast.joinToString(",") { """{"name":${q(it.name)},"character":${q(it.character)},"order":${it.order}}""" }
        val crew = d.crew.joinToString(",") { """{"name":${q(it.name)},"job":${q(it.job)}}""" }
        val studios = d.studios.mapIndexed { i, s -> """{"id":$i,"name":${q(s)}}""" }.joinToString(",")
        val genres = d.genres.mapIndexed { i, g -> """{"id":$i,"name":${q(g)}}""" }.joinToString(",")
        return """{"id":${d.id},"title":${q(d.title)},"original_title":${q(d.originalTitle)},"overview":${q(d.overview)},
            "tagline":${q(d.tagline)},"release_date":"${d.releaseYear}-06-01","runtime":${d.runtimeMinutes},
            "poster_path":${q(d.posterPath)},"genres":[$genres],"production_companies":[$studios],"credits":{"cast":[$cast],"crew":[$crew]}}"""
    }
}
