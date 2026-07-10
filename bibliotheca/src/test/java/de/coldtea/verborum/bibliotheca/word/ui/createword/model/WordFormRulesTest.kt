package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.core.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WordFormRulesTest : BaseTest() {

    // region composeWordText

    @Test
    fun `composeWordText prefixes article from gender and trims text`() {
        val input = WordFormInput(text = " Haus ", gender = Gender.NEUTER)

        assertEquals("das Haus", composeWordText("de", input))
    }

    @Test
    fun `composeWordText returns trimmed text when there is no gender`() {
        val input = WordFormInput(text = " laufen ")

        assertEquals("laufen", composeWordText("de", input))
    }

    @Test
    fun `composeWordText elides the French article before a vowel`() {
        val input = WordFormInput(text = "eau", gender = Gender.FEMININE)

        assertEquals("l'eau", composeWordText("fr", input))
    }

    @Test
    fun `composeWordText adds no article for languages without articles`() {
        val input = WordFormInput(text = "vyras", gender = Gender.MASCULINE)

        assertEquals("vyras", composeWordText("lt", input))
    }

    // endregion

    // region composeWordMeta

    @Test
    fun `composeWordMeta emits bare language for free text without extras`() {
        val input = WordFormInput(text = "Wie geht es dir?")

        assertEquals("{de}", composeWordMeta("de", WordType.FREE_TEXT, input))
    }

    @Test
    fun `composeWordMeta includes type gender and plural for nouns`() {
        val input = WordFormInput(
            text = "Haus",
            gender = Gender.NEUTER,
            fields = mapOf(FieldKey.PLURAL to "Häuser"),
        )

        assertEquals(
            "{de;type=noun;gender=n;plural=Häuser}",
            composeWordMeta("de", WordType.NOUN, input),
        )
    }

    @Test
    fun `composeWordMeta includes feminine for adjectives`() {
        val input = WordFormInput(text = "beau", fields = mapOf(FieldKey.FEMININE to "belle"))

        assertEquals(
            "{fr;type=adjective;feminine=belle}",
            composeWordMeta("fr", WordType.ADJECTIVE, input),
        )
    }

    @Test
    fun `composeWordMeta serializes verb forms in field declaration order`() {
        val input = WordFormInput(
            text = "gehen",
            fields = mapOf(
                FieldKey.AUXILIARY to "sein",
                FieldKey.PARTICIPLE to "gegangen",
                FieldKey.PAST to "ging",
            ),
        )

        assertEquals(
            "{de;type=verb;past=ging;participle=gegangen;aux=sein}",
            composeWordMeta("de", WordType.VERB, input),
        )
    }

    @Test
    fun `composeWordMeta lowercases the language code`() {
        val input = WordFormInput(text = "Haus", gender = Gender.NEUTER)

        assertEquals("{de;type=noun;gender=n}", composeWordMeta("DE", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta omits blank fields and trims values`() {
        val input = WordFormInput(
            text = "Haus",
            gender = Gender.NEUTER,
            fields = mapOf(FieldKey.PLURAL to " Häuser ", FieldKey.PAST to "  "),
        )

        assertEquals("{de;type=noun;gender=n;plural=Häuser}", composeWordMeta("de", WordType.NOUN, input))
    }

    @Test
    fun `composeWordMeta omits type for free text but keeps fields`() {
        val input = WordFormInput(text = "phrase", fields = mapOf(FieldKey.PLURAL to "phrases"))

        assertEquals("{en;plural=phrases}", composeWordMeta("en", WordType.FREE_TEXT, input))
    }

    // endregion
}
