package xyz.engsmyre.moviedescriptiongame.clue

import com.fasterxml.jackson.annotation.JsonInclude

/** Open set: add a value and a builder step to introduce a new kind of clue. */
enum class ClueType { PLOT, TAGLINE, METADATA, PEOPLE }

enum class PersonRole { CAST, DIRECTOR, WRITER }

data class PersonClue(val role: PersonRole, val name: String, val character: String? = null)

/** Type-specific payload; which fields are set depends on [ClueType]. Mirrors `ClueContent` in the API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ClueContent(
    val text: String? = null,
    val genres: List<String>? = null,
    val runtimeMinutes: Int? = null,
    val releaseYear: Int? = null,
    val people: List<PersonClue>? = null,
)

/** A clue as generated and stored (in `movie.clues`). Content is final, spoilers already redacted. */
data class Clue(val id: String, val type: ClueType, val order: Int, val content: ClueContent)
