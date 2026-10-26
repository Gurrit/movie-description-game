package xyz.engsmyre.moviedescriptiongame.clue

import org.springframework.stereotype.Component
import xyz.engsmyre.moviedescriptiongame.config.RedactionProperties
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbMovieDetails

sealed interface ClueResult {
    data class Built(val clues: List<Clue>) : ClueResult
    data class Rejected(val reason: String) : ClueResult
}

/** Turns TMDB details into the stored clues, or says why the movie is not usable. */
@Component
class ClueBuilder(private val props: RedactionProperties) {
    private val redactor = Redactor(props.mask)

    fun build(d: TmdbMovieDetails): ClueResult {
        val overview = d.overview?.trim().orEmpty()
        val tagline = d.tagline?.trim().orEmpty()
        val year = d.releaseYear
        val runtime = d.runtimeMinutes
        val cast = d.cast.sortedBy { it.order }.take(CAST_SIZE)
        val studio = d.studios.firstOrNull()
        val director = d.crew.firstOrNull { it.job == "Director" }
        val writer = WRITER_JOBS.firstNotNullOfOrNull { job -> d.crew.firstOrNull { it.job == job } }

        when {
            d.title.isBlank() -> return rejected("no title")
            overview.isBlank() -> return rejected("no overview")
            tagline.isBlank() -> return rejected("no tagline")
            year == null -> return rejected("no release date")
            runtime == null || runtime <= 0 -> return rejected("no runtime")
            d.genres.isEmpty() -> return rejected("no genres")
            cast.size < CAST_SIZE -> return rejected("fewer than $CAST_SIZE cast members")
            studio == null -> return rejected("no studio")
            director == null -> return rejected("no director")
            writer == null -> return rejected("no writer")
        }

        // Names only: a character's name would give away more than the actor does
        val castNames = cast.map { it.name }
        val titles = listOf(d.title, d.originalTitle).filter { it.isNotBlank() }
        val names = castNames + director!!.name + writer!!.name

        val plot = redactor.redact(overview, titles, names)
        if (plot.maskedRatio > props.maxMaskedRatio) {
            return rejected("overview too redacted (%.0f%%)".format(plot.maskedRatio * 100))
        }
        val tag = redactor.redact(tagline, titles, names)
        if (!tag.hasUnmaskedWords) return rejected("tagline is entirely redacted")

        return ClueResult.Built(
            listOf(
                Clue("metadata", ClueType.METADATA, 1, ClueContent(genres = d.genres, runtimeMinutes = runtime, releaseYear = year)),
                Clue("writer", ClueType.WRITER, 2, ClueContent(names = listOf(writer.name))),
                Clue("studio", ClueType.STUDIO, 3, ClueContent(names = listOf(studio))),
                Clue("director", ClueType.DIRECTOR, 4, ClueContent(names = listOf(director.name))),
                Clue("cast", ClueType.CAST, 5, ClueContent(names = castNames)),
                Clue("tagline", ClueType.TAGLINE, 6, ClueContent(text = tag.text)),
                Clue("plot", ClueType.PLOT, 7, ClueContent(text = plot.text)),
            )
        )
    }

    private fun rejected(reason: String) = ClueResult.Rejected(reason)

    private companion object {
        const val CAST_SIZE = 3
        val WRITER_JOBS = listOf("Screenplay", "Writer", "Story")
    }
}
