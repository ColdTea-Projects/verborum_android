package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageScriptTest {

    @Test
    fun `greek keeps greek letters and drops latin ones`() {
        assertEquals("άνθρωπος", LanguageScript.sanitize("el", "άνθρωποςabc"))
        assertEquals("", LanguageScript.sanitize("el", "hello"))
    }

    @Test
    fun `non-letters always survive so spaces hyphens digits and diacritics pass`() {
        assertEquals("ο άνθρωπος", LanguageScript.sanitize("el", "ο άνθρωπος"))
        assertEquals("ك ت ب", LanguageScript.sanitize("ar", "ك ت ب"))
        // digits and punctuation are script-neutral
        assertEquals("книга-2", LanguageScript.sanitize("ru", "книга-2xyz"))
    }

    @Test
    fun `latin languages keep their accented letters and reject other scripts`() {
        assertEquals("Größe", LanguageScript.sanitize("de", "Größe"))
        assertEquals("Çətənə", LanguageScript.sanitize("az", "Çətənə")) // ə is IPA-extensions Latin
        assertEquals("apple", LanguageScript.sanitize("en", "appleкнига"))
    }

    @Test
    fun `arabic keeps arabic and farsi letters and drops latin`() {
        assertEquals("كتاب", LanguageScript.sanitize("ar", "كتابbook"))
        assertEquals("خریدن", LanguageScript.sanitize("fa", "خریدنbuy")) // Persian letters live in the Arabic block
    }

    @Test
    fun `korean keeps hangul and drops latin`() {
        assertEquals("책", LanguageScript.sanitize("ko", "책book"))
    }

    @Test
    fun `japanese and chinese keep native script AND latin composition letters`() {
        // romaji/pinyin (Latin) must pass so the IME can convert it
        assertEquals("犬inu", LanguageScript.sanitize("ja", "犬inu"))
        assertEquals("书shu", LanguageScript.sanitize("zh", "书shu"))
        // but a genuinely foreign script is still rejected
        assertEquals("书", LanguageScript.sanitize("zh", "书книга"))
    }

    @Test
    fun `unknown language code is left untouched`() {
        assertEquals("anything123абв", LanguageScript.sanitize("xx", "anything123абв"))
        assertFalse(LanguageScript.isRestricted("xx"))
        assertTrue(LanguageScript.isRestricted("el"))
    }
}
