package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.core.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordFormRulesTest : BaseTest() {

    // region articleOptions

    @Test
    fun `articleOptions returns German articles for German nouns`() {
        assertEquals(listOf("der", "die", "das"), articleOptions("de", WordType.NOUN))
    }

    @Test
    fun `articleOptions ignores language code casing`() {
        assertEquals(listOf("der", "die", "das"), articleOptions("DE", WordType.NOUN))
    }

    @Test
    fun `articleOptions is empty for non-noun word types`() {
        assertEquals(emptyList<String>(), articleOptions("de", WordType.VERB))
        assertEquals(emptyList<String>(), articleOptions("de", WordType.ADJECTIVE))
        assertEquals(emptyList<String>(), articleOptions("de", WordType.FREE_TEXT))
    }

    @Test
    fun `articleOptions is empty for languages without articles`() {
        assertEquals(emptyList<String>(), articleOptions("en", WordType.NOUN))
        assertEquals(emptyList<String>(), articleOptions("tr", WordType.NOUN))
    }

    // endregion

    // region field visibility

    @Test
    fun `showsPluralField is true only for nouns`() {
        assertTrue(showsPluralField(WordType.NOUN))
        assertFalse(showsPluralField(WordType.VERB))
        assertFalse(showsPluralField(WordType.ADJECTIVE))
        assertFalse(showsPluralField(WordType.FREE_TEXT))
    }

    @Test
    fun `showsFeminineField is true for adjectives in feminine-adjective languages`() {
        assertTrue(showsFeminineField("fr", WordType.ADJECTIVE))
        assertTrue(showsFeminineField("es", WordType.ADJECTIVE))
        assertTrue(showsFeminineField("it", WordType.ADJECTIVE))
        assertTrue(showsFeminineField("pt", WordType.ADJECTIVE))
    }

    @Test
    fun `showsFeminineField ignores language code casing`() {
        assertTrue(showsFeminineField("FR", WordType.ADJECTIVE))
    }

    @Test
    fun `showsFeminineField is false for adjectives in other languages`() {
        assertFalse(showsFeminineField("de", WordType.ADJECTIVE))
        assertFalse(showsFeminineField("en", WordType.ADJECTIVE))
    }

    @Test
    fun `showsFeminineField is false for non-adjective word types`() {
        assertFalse(showsFeminineField("fr", WordType.NOUN))
        assertFalse(showsFeminineField("fr", WordType.VERB))
        assertFalse(showsFeminineField("fr", WordType.FREE_TEXT))
    }

    // endregion

    // region composeWordText

    @Test
    fun `composeWordText prefixes article and trims text`() {
        val input = WordFormInput(text = " Haus ", article = "das")

        assertEquals("das Haus", composeWordText(input))
    }

    @Test
    fun `composeWordText returns trimmed text when article is null`() {
        val input = WordFormInput(text = " laufen ")

        assertEquals("laufen", composeWordText(input))
    }

    @Test
    fun `composeWordText omits blank article without leading space`() {
        val input = WordFormInput(text = "Haus", article = " ")

        assertEquals("Haus", composeWordText(input))
    }

    // endregion

    // region composeWordMeta

    @Test
    fun `composeWordMeta emits bare language for free text without extras`() {
        val input = WordFormInput(text = "Wie geht es dir?")

        assertEquals("{de}", composeWordMeta("de", WordType.FREE_TEXT, input))
    }

    @Test
    fun `composeWordMeta includes type and plural for nouns`() {
        val input = WordFormInput(text = "Haus", plural = "Häuser")

        assertEquals("{de;type=noun;plural=Häuser}", composeWordMeta("de", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta includes feminine for adjectives`() {
        val input = WordFormInput(text = "beau", feminine = "belle")

        assertEquals(
            "{fr;type=adjective;feminine=belle}",
            composeWordMeta("fr", WordType.ADJECTIVE, input),
        )
    }

    @Test
    fun `composeWordMeta lowercases the language code`() {
        val input = WordFormInput(text = "Haus")

        assertEquals("{de;type=noun}", composeWordMeta("DE", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta omits blank plural and feminine`() {
        val input = WordFormInput(text = "Haus", plural = "  ", feminine = "  ")

        assertEquals("{de;type=noun}", composeWordMeta("de", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta trims plural and feminine values`() {
        val input = WordFormInput(text = "Haus", plural = " Häuser ")

        assertEquals("{de;type=noun;plural=Häuser}", composeWordMeta("de", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta omits type for free text but keeps extras`() {
        val input = WordFormInput(text = "phrase", plural = "phrases")

        assertEquals("{en;plural=phrases}", composeWordMeta("en", WordType.FREE_TEXT, input))
    }

    // endregion
}
