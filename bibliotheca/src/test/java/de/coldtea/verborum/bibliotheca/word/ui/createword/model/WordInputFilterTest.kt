package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.word.ui.createword.model.WordInputFilter.Rejection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WordInputFilterTest {

    private fun filter(
        languageCode: String,
        text: String,
        wordType: WordType = WordType.NOUN,
        fieldKey: FieldKey? = null,
    ) = WordInputFilter.apply(languageCode, wordType, fieldKey, text)

    // region letters only (§3)
    @Test
    fun `letters of the language pass untouched`() {
        val result = filter("de", "Apfel")

        assertEquals("Apfel", result.text)
        assertNull(result.rejection)
    }

    @Test
    fun `digits and punctuation are rejected for every typed word type`() {
        WordType.entries.filter { it != WordType.FREE_TEXT }.forEach { type ->
            val result = filter("en", "book-2.", wordType = type)

            assertEquals("$type kept a non-letter", "book", result.text)
            assertEquals(type.toString(), Rejection.NON_LETTER, result.rejection)
        }
    }

    @Test
    fun `numeral means the word not the digits`() {
        val result = filter("de", "drei3", wordType = WordType.NUMERAL)

        assertEquals("drei", result.text)
    }

    @Test
    fun `symbols the doc calls out are all rejected`() {
        val result = filter("en", "a+b=c{d}e<f>g|h\\i")

        assertEquals("abcdefghi", result.text)
        assertEquals(Rejection.NON_LETTER, result.rejection)
    }

    @Test
    fun `line breaks and control characters never survive`() {
        assertEquals("ab", filter("en", "a\nb\t").text)
    }
    // endregion

    // region script (§3)
    @Test
    fun `a letter from another script is reported as a script rejection`() {
        val result = filter("el", "άνθρωποςabc")

        assertEquals("άνθρωπος", result.text)
        assertEquals(Rejection.FOREIGN_SCRIPT, result.rejection)
    }

    @Test
    fun `a foreign letter outranks punctuation in the reported reason`() {
        // The script message names the language, which is the more useful thing to say.
        assertEquals(Rejection.FOREIGN_SCRIPT, filter("el", "άb.").rejection)
    }

    @Test
    fun `accented latin letters survive across the latin languages`() {
        assertEquals("Größe", filter("de", "Größe").text)
        assertEquals("Çətənə", filter("az", "Çətənə").text)
        assertEquals("łódź", filter("pl", "łódź").text)
        assertEquals("ação", filter("pt", "ação").text)
    }

    @Test
    fun `non-latin scripts survive`() {
        assertEquals("книга", filter("ru", "книга").text)
        assertEquals("كتاب", filter("ar", "كتاب").text)
        assertEquals("책", filter("ko", "책").text)
        assertEquals("犬", filter("ja", "犬").text)
        assertEquals("书", filter("zh", "书").text)
    }

    @Test
    fun `a decomposed diacritic keeps its combining mark`() {
        // "u" + combining diaeresis (U+0308) — dropping the mark would silently change the word.
        val decomposed = "Gru\u0308ße"

        assertEquals(decomposed, filter("de", decomposed).text)
    }
    // endregion

    // region composition characters (§4)
    @Test
    fun `the space and apostrophe that composition adds are not typeable`() {
        // "der Apfel" / "l'eau" are composed after the fact; the typed field is the base word.
        assertEquals("derApfel", filter("de", "der Apfel").text)
        assertEquals("leau", filter("fr", "l'eau").text)
    }
    // endregion

    // region free text (§5)
    @Test
    fun `free text is never filtered`() {
        val messy = "Anything! 123 — книга + 犬 \n"

        val result = filter("de", messy, wordType = WordType.FREE_TEXT)

        assertEquals(messy, result.text)
        assertNull(result.rejection)
    }
    // endregion

    // region composing IMEs (§6)
    @Test
    fun `japanese chinese and korean defer validation to commit`() {
        assertTrue(WordInputFilter.defersToCommit("ja"))
        assertTrue(WordInputFilter.defersToCommit("zh"))
        assertTrue(WordInputFilter.defersToCommit("ko"))
        assertTrue(WordInputFilter.defersToCommit("JA"))
    }

    @Test
    fun `per-keystroke scripts do not defer`() {
        assertFalse(WordInputFilter.defersToCommit("de"))
        assertFalse(WordInputFilter.defersToCommit("el"))
        assertFalse(WordInputFilter.defersToCommit("ar"))
    }

    @Test
    fun `romaji and pinyin survive as the composition alphabet`() {
        assertEquals("inu", filter("ja", "inu").text)
        assertEquals("shu", filter("zh", "shu").text)
    }
    // endregion

    // region field-level exceptions (§7)
    @Test
    fun `the arabic root keeps the spaces it is stored with`() {
        val result = filter("ar", "ك ت ب", fieldKey = FieldKey.ROOT)

        assertEquals("ك ت ب", result.text)
        assertNull(result.rejection)
    }

    @Test
    fun `the space allowance is scoped to the arabic root alone`() {
        // Another Arabic field, and another language's root, both still reject the space.
        assertEquals("كتاب", filter("ar", "كت اب", fieldKey = FieldKey.PLURAL).text)
        assertEquals("كتاب", filter("ar", "كت اب", fieldKey = null).text)
        assertEquals("kitab", filter("en", "kit ab", fieldKey = FieldKey.ROOT).text)
    }

    @Test
    fun `the reading field accepts anything the user types`() {
        // A pronunciation note the user writes for themselves, in their own keyboard: no script,
        // no letters-only rule. Tone-marked pinyin, kana, Latin, punctuation, all of it stands.
        val messy = "shū / mǎi (books!) 本"

        val result = filter("zh", messy, fieldKey = FieldKey.READING)

        assertEquals(messy, result.text)
        assertNull(result.rejection)
    }

    @Test
    fun `the reading field is unfiltered in every language`() {
        FieldKey.entries.forEach { key ->
            assertEquals(
                "only READING may be unfiltered",
                key == FieldKey.READING,
                WordInputFilter.isUnfiltered(key),
            )
        }
        assertFalse(WordInputFilter.isUnfiltered(null))
        assertEquals("kana かな 123", filter("ja", "kana かな 123", fieldKey = FieldKey.READING).text)
    }

    @Test
    fun `grammatical form fields are filtered like the base word`() {
        val result = filter("de", "Äpfel!", fieldKey = FieldKey.PLURAL)

        assertEquals("Äpfel", result.text)
        assertEquals(Rejection.NON_LETTER, result.rejection)
    }
    // endregion

    // region character limits
    @Test
    fun `words and forms are capped at 40 characters`() {
        val long = "a".repeat(41)

        val baseWord = filter("de", long)
        assertEquals(WordInputFilter.MAX_TEXT_LENGTH, baseWord.text.length)
        assertEquals(long.take(40), baseWord.text)

        val plural = filter("de", long, fieldKey = FieldKey.PLURAL)
        assertEquals(WordInputFilter.MAX_TEXT_LENGTH, plural.text.length)
    }

    @Test
    fun `a cap does not trim within the limit`() {
        assertEquals("Apfel", filter("de", "Apfel").text)
    }

    @Test
    fun `free text is capped at 150 characters`() {
        val long = "x".repeat(151)

        val reading = filter("de", long, fieldKey = FieldKey.READING)
        assertEquals(WordInputFilter.FREE_TEXT_MAX_LENGTH, reading.text.length)

        val freeText = filter("de", long, wordType = WordType.FREE_TEXT)
        assertEquals(WordInputFilter.FREE_TEXT_MAX_LENGTH, freeText.text.length)
    }

    @Test
    fun `free text within the limit passes untouched`() {
        val messy = "Anything! 123 — книга + 犬 \n".repeat(3)
        assertTrue(messy.length <= WordInputFilter.FREE_TEXT_MAX_LENGTH)

        val result = filter("de", messy, wordType = WordType.FREE_TEXT)

        assertEquals(messy, result.text)
        assertNull(result.rejection)
    }

    @Test
    fun `a cap never splits a surrogate pair`() {
        val result = filter("en", "🙂".repeat(151), wordType = WordType.FREE_TEXT)

        assertEquals(
            WordInputFilter.FREE_TEXT_MAX_LENGTH,
            result.text.codePointCount(0, result.text.length),
        )
        assertEquals("🙂".repeat(150), result.text)
    }
    // endregion

    @Test
    fun `empty input is accepted so the field can be cleared`() {
        val result = filter("de", "")

        assertEquals("", result.text)
        assertNull(result.rejection)
    }
}
