package de.coldtea.verborum.bibliotheca.word.ui.model

import de.coldtea.verborum.bibliotheca.word.ui.createword.model.FieldKey
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.Gender
import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordType
import de.coldtea.verborum.core.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WordMetaTest : BaseTest() {

    // region parse

    @Test
    fun `parse reads language type gender and fields from a full meta`() {
        val meta = WordMeta.parse("{de;type=verb;gender=m;past=ging;participle=gegangen;aux=sein}")

        assertEquals("de", meta?.languageCode)
        assertEquals(WordType.VERB, meta?.wordType)
        assertEquals(Gender.MASCULINE, meta?.gender)
        assertEquals("ging", meta?.field(FieldKey.PAST))
        assertEquals("gegangen", meta?.field(FieldKey.PARTICIPLE))
        assertEquals("sein", meta?.field(FieldKey.AUXILIARY))
    }

    @Test
    fun `parse reads a legacy meta without gender`() {
        val meta = WordMeta.parse("{de;type=noun;plural=Häuser}")

        assertEquals("de", meta?.languageCode)
        assertEquals(WordType.NOUN, meta?.wordType)
        assertNull(meta?.gender)
        assertEquals("Häuser", meta?.field(FieldKey.PLURAL))
    }

    @Test
    fun `parse reads a bare language meta`() {
        val meta = WordMeta.parse("{en}")

        assertEquals("en", meta?.languageCode)
        assertNull(meta?.wordType)
        assertNull(meta?.gender)
        assertEquals(emptyMap<FieldKey, String>(), meta?.fields)
    }

    @Test
    fun `parse returns null for blank or empty meta`() {
        assertNull(WordMeta.parse(""))
        assertNull(WordMeta.parse("   "))
        assertNull(WordMeta.parse("{}"))
    }

    @Test
    fun `parse ignores unknown keys`() {
        val meta = WordMeta.parse("{de;type=noun;futurefeature=x;plural=Häuser}")

        assertEquals("Häuser", meta?.field(FieldKey.PLURAL))
        assertEquals(1, meta?.fields?.size)
    }

    // endregion

    // region displayForm

    @Test
    fun `displayForm prefixes the participle with its auxiliary`() {
        val meta = WordMeta.parse("{de;type=verb;participle=gegangen;aux=sein}")

        assertEquals("(sein) gegangen", meta?.displayForm(FieldKey.PARTICIPLE))
    }

    @Test
    fun `displayForm returns the bare participle without an auxiliary`() {
        val meta = WordMeta.parse("{en;type=verb;participle=gone}")

        assertEquals("gone", meta?.displayForm(FieldKey.PARTICIPLE))
    }

    @Test
    fun `displayForm never exposes the auxiliary as its own form`() {
        val meta = WordMeta.parse("{de;type=verb;aux=sein}")

        assertNull(meta?.displayForm(FieldKey.AUXILIARY))
    }

    // endregion

    // region displayLine

    @Test
    fun `displayLine appends every form to the stored text`() {
        assertEquals(
            "gehen · ging · (sein) gegangen",
            displayLine("gehen", "{de;type=verb;past=ging;participle=gegangen;aux=sein}"),
        )
    }

    @Test
    fun `displayLine shows the noun with its plural`() {
        assertEquals(
            "der Apfel · Äpfel",
            displayLine("der Apfel", "{de;type=noun;gender=m;plural=Äpfel}"),
        )
    }

    @Test
    fun `displayLine returns the bare text when the meta has no forms`() {
        assertEquals("elma", displayLine("elma", "{tr}"))
        assertEquals("elma", displayLine("elma", ""))
    }

    // endregion
}
