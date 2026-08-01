package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageScriptTest {

    /** Every letter of [text] is one this language writes with. */
    private fun allowsAll(languageCode: String, text: String): Boolean =
        text.codePoints().allMatch { LanguageScript.allowsLetter(languageCode, it) }

    private fun allowsNone(languageCode: String, text: String): Boolean =
        text.codePoints().noneMatch { LanguageScript.allowsLetter(languageCode, it) }

    @Test
    fun `greek accepts greek letters and rejects latin ones`() {
        assertTrue(allowsAll("el", "άνθρωπος"))
        assertTrue(allowsNone("el", "hello"))
    }

    @Test
    fun `non-letters are not this object's business and always pass`() {
        // Whether a space or a digit may be typed is WordInputFilter's call, not the script's.
        assertTrue(allowsAll("el", " -2.'"))
    }

    @Test
    fun `latin languages accept their accented letters and reject other scripts`() {
        assertTrue(allowsAll("de", "Größe"))
        assertTrue(allowsAll("az", "Çətənə")) // ə is IPA-extensions Latin
        assertTrue(allowsNone("en", "книга"))
    }

    @Test
    fun `arabic accepts arabic and farsi letters and rejects latin`() {
        assertTrue(allowsAll("ar", "كتاب"))
        assertTrue(allowsAll("fa", "خریدن")) // Persian letters live in the Arabic block
        assertTrue(allowsNone("ar", "book"))
    }

    @Test
    fun `korean accepts hangul and rejects latin`() {
        assertTrue(allowsAll("ko", "책"))
        assertTrue(allowsNone("ko", "book"))
    }

    @Test
    fun `japanese and chinese accept native script AND latin composition letters`() {
        // romaji/pinyin (Latin) must pass so the IME can convert it
        assertTrue(allowsAll("ja", "犬inu"))
        assertTrue(allowsAll("zh", "书shu"))
        // but a genuinely foreign script is still rejected
        assertTrue(allowsNone("zh", "книга"))
    }

    @Test
    fun `unknown language code accepts every letter`() {
        assertTrue(allowsAll("xx", "anythingабв"))
        assertFalse(LanguageScript.isRestricted("xx"))
        assertTrue(LanguageScript.isRestricted("el"))
    }
}
