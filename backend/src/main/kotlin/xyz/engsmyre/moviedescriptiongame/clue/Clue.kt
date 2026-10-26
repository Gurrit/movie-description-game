package xyz.engsmyre.moviedescriptiongame.clue

import com.fasterxml.jackson.annotation.JsonInclude

/** Open set: add a value and a builder step to introduce a new kind of clue. */
enum class ClueType { METADATA, WRITER, STUDIO, DIRECTOR, CAST, TAGLINE, PLOT }

/** Type-specific payload; which fields are set depends on [ClueType]. Mirrors `ClueContent` in the API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClueContent(
    val text: String? = null,
    val genres: List<String>? = null,
    val runtimeMinutes: Int? = null,
    val releaseYear: Int? = null,
    /** Real names of the people of a WRITER, DIRECTOR or CAST clue (never the characters they play), or the studio of a STUDIO clue. */
    val names: List<String>? = null,
)

/**
 * A clue as generated and stored (in `movie.clues`). Content is final, spoilers already redacted.
 * [order] is also the unlock order: the first clue is shown from the start, each wrong guess unlocks the next.
 */
data class Clue(val id: String, val type: ClueType, val order: Int, val content: ClueContent)
