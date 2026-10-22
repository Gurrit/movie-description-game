package xyz.engsmyre.moviedescriptiongame.clue

import java.text.Normalizer

/**
 * Masks spoilers (the movie's title and the names of people that are a separate clue) in free text.
 * Matching is case-, accent- and punctuation-insensitive.
 */
class Redactor(private val mask: String) {

    data class Result(val text: String, val maskedWords: Int, val totalWords: Int) {
        val maskedRatio: Double get() = if (totalWords == 0) 0.0 else maskedWords.toDouble() / totalWords
        val hasUnmaskedWords: Boolean get() = totalWords > maskedWords
    }

    private data class Token(val start: Int, val end: Int, val norm: String)

    fun redact(text: String, titles: Collection<String>, names: Collection<String>): Result {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return Result(text, 0, 0)

        val phrases = (titles + names).map { tokenize(it).map(Token::norm) }.filter { it.isNotEmpty() }
            .distinct().sortedByDescending { it.size }
        val words = buildSet {
            for (title in titles) {
                val parts = tokenize(title).map(Token::norm)
                // A one-word title is masked whatever it is, otherwise it would leak the answer
                if (parts.size == 1) add(parts.single()) else parts.filterTo(this) { it.isMaskableWord() }
            }
            for (name in names) tokenize(name).map(Token::norm).filterTo(this) { it.isMaskableWord() }
        }

        val sb = StringBuilder()
        var cursor = 0
        var maskedWords = 0
        var i = 0
        while (i < tokens.size) {
            val phrase = phrases.firstOrNull { p -> p.size > 1 && matchesAt(tokens, i, p) }
            val length = when {
                phrase != null -> phrase.size
                tokens[i].norm in words || phrases.any { it.size == 1 && it[0] == tokens[i].norm } -> 1
                else -> 0
            }
            if (length > 0) {
                sb.append(text, cursor, tokens[i].start).append(mask)
                cursor = tokens[i + length - 1].end
                maskedWords += length
                i += length
            } else {
                i++
            }
        }
        sb.append(text, cursor, text.length)
        return Result(sb.toString(), maskedWords, tokens.size)
    }

    private fun matchesAt(tokens: List<Token>, at: Int, phrase: List<String>): Boolean =
        at + phrase.size <= tokens.size && phrase.indices.all { tokens[at + it].norm == phrase[it] }

    private fun String.isMaskableWord() = length > 2 && this !in STOPWORDS

    private fun tokenize(text: String): List<Token> =
        WORD.findAll(text).map { Token(it.range.first, it.range.last + 1, normalize(it.value)) }.toList()

    private fun normalize(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(MARKS, "").lowercase()

    private companion object {
        val WORD = Regex("[\\p{L}\\p{N}]+")
        val MARKS = Regex("\\p{M}+")
        val STOPWORDS = setOf(
            "the", "a", "an", "of", "and", "or", "in", "on", "at", "to", "for", "with", "from", "by", "is", "it", "as",
        )
    }
}
