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
    fun `builds the four clues in order`() {
        val clues = built(builder.build(Fixtures.details(7)))
        assertEquals(listOf("plot", "tagline", "metadata", "people"), clues.map { it.id })
        assertEquals(listOf(1, 2, 3, 4), clues.map { it.order })
        assertEquals(listOf(ClueType.PLOT, ClueType.TAGLINE, ClueType.METADATA, ClueType.PEOPLE), clues.map { it.type })
    }

    @Test
    fun `people clue has three cast members, director and writer`() {
        val people = built(builder.build(Fixtures.details(7))).single { it.type == ClueType.PEOPLE }.content.people!!
        assertEquals(listOf(PersonRole.CAST, PersonRole.CAST, PersonRole.CAST, PersonRole.DIRECTOR, PersonRole.WRITER), people.map { it.role })
        assertEquals("Alice Actor7", people[0].name)
        assertEquals("Hero", people[0].character)
        assertEquals("Dora Director7", people[3].name)
        assertEquals("Wally Writer7", people[4].name)
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
    fun `plot hides the title and the cast and crew names`() {
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
        val people = built(builder.build(d)).single { it.type == ClueType.PEOPLE }.content.people!!
        assertEquals("W Writ", people.last().name)
    }

    @Test
    fun `rejects movies missing required data`() {
        assertEquals("no overview", reason(builder.build(Fixtures.details(1, overview = " "))))
        assertEquals("no tagline", reason(builder.build(Fixtures.details(1, tagline = null))))
        assertEquals("fewer than 3 cast members", reason(builder.build(Fixtures.details(1, cast = emptyList()))))
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
