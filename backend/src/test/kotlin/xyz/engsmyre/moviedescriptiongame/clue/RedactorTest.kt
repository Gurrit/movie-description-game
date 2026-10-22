package xyz.engsmyre.moviedescriptiongame.clue

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RedactorTest {
    private val redactor = Redactor("###")

    @Test
    fun `masks the full title phrase ignoring case and punctuation`() {
        val r = redactor.redact("In the film Star Wars, a farm boy joins the rebels.", listOf("Star Wars"), emptyList())
        assertEquals("In the film ###, a farm boy joins the rebels.", r.text)
        assertEquals(2, r.maskedWords)
    }

    @Test
    fun `masks single title words but not stopwords`() {
        val r = redactor.redact("Old Godfather hands over the empire.", listOf("The Godfather"), emptyList())
        assertEquals("Old ### hands over the empire.", r.text)
    }

    @Test
    fun `a one word title is always masked`() {
        val r = redactor.redact("Alien is a horror film. An alien hunts the crew.", listOf("Alien"), emptyList())
        assertEquals("### is a horror film. An ### hunts the crew.", r.text)
    }

    @Test
    fun `masks accents and possessives`() {
        val r = redactor.redact("Amélie's quiet life changes.", listOf("Amelie"), emptyList())
        assertEquals("###'s quiet life changes.", r.text)
    }

    @Test
    fun `masks full names and name parts`() {
        val r = redactor.redact("Directed by Ridley Scott. Scott cast Sigourney Weaver.", emptyList(), listOf("Ridley Scott", "Sigourney Weaver"))
        assertEquals("Directed by ###. ### cast ###.", r.text)
    }

    @Test
    fun `short name parts are left alone`() {
        val r = redactor.redact("Jo is at the door.", emptyList(), listOf("Jo Smith"))
        assertTrue(r.text.startsWith("Jo is at"))
    }

    @Test
    fun `reports the ratio of masked words`() {
        val r = redactor.redact("Alien Alien run run", listOf("Alien"), emptyList())
        assertEquals(0.5, r.maskedRatio, 0.0001)
        assertTrue(r.hasUnmaskedWords)
    }

    @Test
    fun `text that is only the title has no unmasked words`() {
        val r = redactor.redact("The Dark Knight", listOf("The Dark Knight"), emptyList())
        assertEquals("###", r.text)
        assertFalse(r.hasUnmaskedWords)
    }

    @Test
    fun `text without tokens is returned unchanged`() {
        val r = redactor.redact("...", listOf("Alien"), emptyList())
        assertEquals("...", r.text)
        assertEquals(0.0, r.maskedRatio, 0.0)
    }
}
