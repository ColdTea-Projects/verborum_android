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
        val meta = WordMeta.parse(
            """{"lang":"de","type":"verb","genders":["m"],"fields":{"past":["ging"],"participle":["gegangen"],"aux":["sein"]}}"""
        )

        assertEquals("de", meta?.languageCode)
        assertEquals(WordType.VERB, meta?.wordType)
        assertEquals(Gender.MASCULINE, meta?.gender)
        assertEquals("ging", meta?.field(FieldKey.PAST))
        assertEquals("gegangen", meta?.field(FieldKey.PARTICIPLE))
        assertEquals("sein", meta?.field(FieldKey.AUXILIARY))
    }

    @Test
    fun `parse reads a meta without gender`() {
        val meta = WordMeta.parse("""{"lang":"de","type":"noun","fields":{"plural":["Häuser"]}}""")

        assertEquals("de", meta?.languageCode)
        assertEquals(WordType.NOUN, meta?.wordType)
        assertNull(meta?.gender)
        assertEquals("Häuser", meta?.field(FieldKey.PLURAL))
    }

    @Test
    fun `parse reads a bare language meta`() {
        val meta = WordMeta.parse("""{"lang":"en"}""")

        assertEquals("en", meta?.languageCode)
        assertNull(meta?.wordType)
        assertNull(meta?.gender)
        assertEquals(emptyMap<FieldKey, String>(), meta?.fields)
    }

    @Test
    fun `parse returns null for blank or malformed meta`() {
        assertNull(WordMeta.parse(""))
        assertNull(WordMeta.parse("   "))
        assertNull(WordMeta.parse("{}"))
        assertNull(WordMeta.parse("{de;type=noun}"))
    }

    @Test
    fun `parse ignores unknown keys`() {
        val meta = WordMeta.parse(
            """{"lang":"de","type":"noun","futurefeature":1,"fields":{"futureform":["x"],"plural":["Häuser"]}}"""
        )

        assertEquals("Häuser", meta?.field(FieldKey.PLURAL))
        assertEquals(1, meta?.fields?.size)
    }

    @Test
    fun `parse takes the first meaning when the meta has several`() {
        val meta = WordMeta.parse(
            """{"lang":"de","type":"verb","fields":{"past":["kaufte","erwarb"]}}"""
        )

        assertEquals("kaufte", meta?.field(FieldKey.PAST))
    }

    // endregion

    // region parseBundle

    @Test
    fun `parseBundle aligns each meaning with its own gender and fields`() {
        val bundle = parseBundle(
            """{"lang":"de","type":"noun","genders":["m","f"],"fields":{"plural":["Seen",""]}}"""
        )

        assertEquals(2, bundle?.meanings?.size)
        assertEquals(Gender.MASCULINE, bundle?.meanings?.get(0)?.gender)
        assertEquals("Seen", bundle?.meanings?.get(0)?.fields?.get(FieldKey.PLURAL))
        assertEquals(Gender.FEMININE, bundle?.meanings?.get(1)?.gender)
        assertNull(bundle?.meanings?.get(1)?.fields?.get(FieldKey.PLURAL))
    }

    // endregion

    // region surfaces

    @Test
    fun `splitSurfaces reads the json array and keeps slashes inside words intact`() {
        assertEquals(listOf("and/or", "either"), splitSurfaces("""["and/or","either"]"""))
    }

    @Test
    fun `splitSurfaces treats a non-array value as one bare surface`() {
        assertEquals(listOf("das Haus"), splitSurfaces("das Haus"))
        assertEquals(emptyList<String>(), splitSurfaces("  "))
    }

    @Test
    fun `surfacesDisplay joins the stored surfaces with a slash`() {
        assertEquals("buy/purchase", surfacesDisplay("""["buy","purchase"]""", "en"))
        assertEquals("elma", surfacesDisplay("""["elma"]""", "tr"))
    }

    // endregion

    // region displayForm

    @Test
    fun `displayForm prefixes the participle with its auxiliary`() {
        val meta = WordMeta.parse("""{"lang":"de","type":"verb","fields":{"participle":["gegangen"],"aux":["sein"]}}""")

        assertEquals("(sein) gegangen", meta?.displayForm(FieldKey.PARTICIPLE))
    }

    @Test
    fun `displayForm returns the bare participle without an auxiliary`() {
        val meta = WordMeta.parse("""{"lang":"en","type":"verb","fields":{"participle":["gone"]}}""")

        assertEquals("gone", meta?.displayForm(FieldKey.PARTICIPLE))
    }

    @Test
    fun `displayForm never exposes the auxiliary as its own form`() {
        val meta = WordMeta.parse("""{"lang":"de","type":"verb","fields":{"aux":["sein"]}}""")

        assertNull(meta?.displayForm(FieldKey.AUXILIARY))
    }

    // endregion

    // region displayLine

    @Test
    fun `displayLine appends every form to the stored text`() {
        assertEquals(
            "gehen · ging · (sein) gegangen",
            displayLine(
                """["gehen"]""",
                """{"lang":"de","type":"verb","fields":{"past":["ging"],"participle":["gegangen"],"aux":["sein"]}}""",
            ),
        )
    }

    @Test
    fun `displayLine shows the noun with its plural`() {
        assertEquals(
            "der Apfel · Äpfel",
            displayLine(
                """["der Apfel"]""",
                """{"lang":"de","type":"noun","genders":["m"],"fields":{"plural":["Äpfel"]}}""",
            ),
        )
    }

    @Test
    fun `displayLine returns the bare text when the meta has no forms`() {
        assertEquals("elma", displayLine("""["elma"]""", """{"lang":"tr"}"""))
        assertEquals("elma", displayLine("elma", ""))
    }

    @Test
    fun `displayLine joins alternative meanings with a slash in each slot`() {
        assertEquals(
            "kaufen/erwerben · kaufte/erwarb · (haben) gekauft/(haben) erworben",
            displayLine(
                """["kaufen","erwerben"]""",
                """{"lang":"de","type":"verb","fields":{"past":["kaufte","erwarb"],"participle":["gekauft","erworben"],"aux":["haben","haben"]}}""",
            ),
        )
    }

    @Test
    fun `displayLine shows alternative surfaces of a formless meta`() {
        assertEquals(
            "considerable/significant",
            displayLine("""["considerable","significant"]""", """{"lang":"en","type":"adjective"}"""),
        )
    }

    // endregion
}
