package xyz.engsmyre.moviedescriptiongame.clue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import xyz.engsmyre.moviedescriptiongame.config.RedactionProperties
import xyz.engsmyre.moviedescriptiongame.support.Fixtures
import xyz.engsmyre.moviedescriptiongame.tmdb.TmdbCrewMember

class ClueBuilderTest {
    private val builder = ClueBuilder(RedactionProperties(maxMaskedRatio = 0.35, mask = "###"))

    private fun built(result: ClueResult) = (result as? ClueResult.Built)?.clues ?: error("expected Built but was $result")
    private fun reason(result: ClueResult) = (result as? ClueResult.Rejected)?.reason ?: error("expected Rejected")

    @Test
    fun `builds the seven clues in unlock order`() {
        val clues = built(builder.build(Fixtures.details(7)))
        assertEquals(listOf("metadata", "writer", "studio", "director", "cast", "tagline", "plot"), clues.map { it.id })
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), clues.map { it.order })
        assertEquals(
            listOf(ClueType.METADATA, ClueType.WRITER, ClueType.STUDIO, ClueType.DIRECTOR, ClueType.CAST, ClueType.TAGLINE, ClueType.PLOT),
            clues.map { it.type },
        )
    }

    @Test
    fun `writer director and cast clues carry names only`() {
        val clues = built(builder.build(Fixtures.details(7)))
        fun names(type: ClueType) = clues.single { it.type == type }.content.names
        assertEquals(listOf("Wally Writer7"), names(ClueType.WRITER))
        assertEquals(listOf("Studio 7 Pictures"), names(ClueType.STUDIO), "the first production company")
        assertEquals(listOf("Dora Director7"), names(ClueType.DIRECTOR))
        // the three top-billed actors; no characters (Hero, Sidekick, Villain) anywhere
        assertEquals(listOf("Alice Actor7", "Bob Actor7", "Carol Actor7"), names(ClueType.CAST))
        val everything = clues.joinToString { it.content.toString() }
        for (character in listOf("Hero", "Sidekick", "Villain", "Bystander")) assertFalse(everything.contains(character), everything)
    }

    @Test
    fun `metadata clue carries genres runtime and year`() {
        val d = Fixtures.details(7)
        val content = built(builder.build(d)).single { it.type == ClueType.METADATA }.content
        assertEquals(d.genres, content.genres)
        assertEquals(d.runtimeMinutes, content.runtimeMinutes)
        assertEquals(d.releaseYear, content.releaseYear)
    }

    @Test
    fun `plot hides the title and the names of cast and crew`() {
        val d = Fixtures.details(
            7, title = "Zorblax",
            overview = "Zorblax follows Dora Director7 and Alice Actor7 across a wide and lonely desert, hoping to find water before the long winter arrives.",
        )
        val plot = built(builder.build(d)).single { it.type == ClueType.PLOT }.content.text!!
        assertFalse(plot.contains("Zorblax", ignoreCase = true), plot)
        assertFalse(plot.contains("Dora"), plot)
        assertFalse(plot.contains("Alice"), plot)
        assertTrue(plot.contains("###"))
    }

    @Test
    fun `writer falls back through Screenplay Writer Story`() {
        val d = Fixtures.details(7, crew = listOf(TmdbCrewMember("D Dir", "Director"), TmdbCrewMember("S Story", "Story"), TmdbCrewMember("W Writ", "Writer")))
        val writer = built(builder.build(d)).single { it.type == ClueType.WRITER }.content.names
        assertEquals(listOf("W Writ"), writer)
    }

    @Test
    fun `rejects movies missing required data`() {
        assertEquals("no overview", reason(builder.build(Fixtures.details(1, overview = " "))))
        assertEquals("no tagline", reason(builder.build(Fixtures.details(1, tagline = null))))
        assertEquals("fewer than 3 cast members", reason(builder.build(Fixtures.details(1, cast = emptyList()))))
        assertEquals("no studio", reason(builder.build(Fixtures.details(1, studios = emptyList()))))
        assertEquals("no director", reason(builder.build(Fixtures.details(1, crew = listOf(TmdbCrewMember("W", "Writer"))))))
        assertEquals("no writer", reason(builder.build(Fixtures.details(1, crew = listOf(TmdbCrewMember("D", "Director"))))))
    }

    @Test
    fun `rejects an overview that is mostly redacted`() {
        val d = Fixtures.details(1, title = "Run Run", overview = "Run run run run to the hills now")
        assertTrue(reason(builder.build(d)).startsWith("overview too redacted"))
    }

    @Test
    fun `rejects a tagline that is only the title`() {
        val d = Fixtures.details(1, title = "Zorblax", tagline = "Zorblax")
        assertEquals("tagline is entirely redacted", reason(builder.build(d)))
    }
}
