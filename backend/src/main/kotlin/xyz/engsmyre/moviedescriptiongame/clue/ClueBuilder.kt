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
            director == null -> return rejected("no director")
            writer == null -> return rejected("no writer")
        }

        val people = cast.map { PersonClue(PersonRole.CAST, it.name, it.character?.takeIf(String::isNotBlank)) } +
            PersonClue(PersonRole.DIRECTOR, director!!.name) +
            PersonClue(PersonRole.WRITER, writer!!.name)
        val titles = listOf(d.title, d.originalTitle).filter { it.isNotBlank() }
        val names = people.map { it.name }

        val plot = redactor.redact(overview, titles, names)
        if (plot.maskedRatio > props.maxMaskedRatio) {
            return rejected("overview too redacted (%.0f%%)".format(plot.maskedRatio * 100))
        }
        val tag = redactor.redact(tagline, titles, names)
        if (!tag.hasUnmaskedWords) return rejected("tagline is entirely redacted")

        return ClueResult.Built(
            listOf(
                Clue("plot", ClueType.PLOT, 1, ClueContent(text = plot.text)),
                Clue("tagline", ClueType.TAGLINE, 2, ClueContent(text = tag.text)),
                Clue(
                    "metadata", ClueType.METADATA, 3,
                    ClueContent(genres = d.genres, runtimeMinutes = runtime, releaseYear = year),
                ),
                Clue("people", ClueType.PEOPLE, 4, ClueContent(people = people)),
            )
        )
    }

    private fun rejected(reason: String) = ClueResult.Rejected(reason)

    private companion object {
        const val CAST_SIZE = 3
        val WRITER_JOBS = listOf("Screenplay", "Writer", "Story")
    }
}
