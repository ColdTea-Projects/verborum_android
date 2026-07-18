package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import java.lang.Character.UnicodeBlock

/**
 * Restricts word input to the script of each dictionary language, so a Greek side accepts only
 * Greek letters, an Arabic side only Arabic letters, and so on. Only *letters* are constrained —
 * whitespace, digits, punctuation and combining marks are script-neutral and always pass, so
 * spaces, hyphens and diacritics keep working everywhere.
 *
 * Japanese and Chinese are special: their keyboards type romaji/pinyin (Latin) that the IME
 * converts to kana/hanzi, so Latin is permitted there as the composition alphabet — other foreign
 * scripts are still rejected. Unknown language codes are left unfiltered.
 */
object LanguageScript {

    // Only long-standing UnicodeBlock constants are used (present since early Android / minSdk 23).
    private val LATIN_BLOCKS = setOf(
        UnicodeBlock.BASIC_LATIN,
        UnicodeBlock.LATIN_1_SUPPLEMENT,
        UnicodeBlock.LATIN_EXTENDED_A,
        UnicodeBlock.LATIN_EXTENDED_B,
        UnicodeBlock.LATIN_EXTENDED_ADDITIONAL,
        UnicodeBlock.IPA_EXTENSIONS, // Azerbaijani ə lives here
        UnicodeBlock.SPACING_MODIFIER_LETTERS,
    )

    private val GREEK_BLOCKS = setOf(UnicodeBlock.GREEK, UnicodeBlock.GREEK_EXTENDED)

    private val CYRILLIC_BLOCKS = setOf(UnicodeBlock.CYRILLIC, UnicodeBlock.CYRILLIC_SUPPLEMENTARY)

    private val ARABIC_BLOCKS = setOf(
        UnicodeBlock.ARABIC,
        UnicodeBlock.ARABIC_SUPPLEMENT, // Farsi's پ چ ژ گ ک ی already sit in the base Arabic block
        UnicodeBlock.ARABIC_PRESENTATION_FORMS_A,
        UnicodeBlock.ARABIC_PRESENTATION_FORMS_B,
    )

    private val HANGUL_BLOCKS = setOf(
        UnicodeBlock.HANGUL_SYLLABLES,
        UnicodeBlock.HANGUL_JAMO, // half-formed syllables while the IME composes
        UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
    )

    private val CJK_BLOCKS = setOf(
        UnicodeBlock.HIRAGANA,
        UnicodeBlock.KATAKANA,
        UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS,
        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION,
        UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS,
        UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS,
        UnicodeBlock.BOPOMOFO,
    )

    // Japanese/Chinese also accept Latin, the romaji/pinyin the IME converts from.
    private val allowedByLanguage: Map<String, Set<UnicodeBlock>> = mapOf(
        "en" to LATIN_BLOCKS, "de" to LATIN_BLOCKS, "fr" to LATIN_BLOCKS, "es" to LATIN_BLOCKS,
        "it" to LATIN_BLOCKS, "pt" to LATIN_BLOCKS, "nl" to LATIN_BLOCKS, "lt" to LATIN_BLOCKS,
        "tr" to LATIN_BLOCKS, "az" to LATIN_BLOCKS,
        "el" to GREEK_BLOCKS,
        "ru" to CYRILLIC_BLOCKS, "uk" to CYRILLIC_BLOCKS,
        "ar" to ARABIC_BLOCKS, "fa" to ARABIC_BLOCKS,
        "ko" to HANGUL_BLOCKS,
        "ja" to CJK_BLOCKS + LATIN_BLOCKS,
        "zh" to CJK_BLOCKS + LATIN_BLOCKS,
    )

    /** True when the language enforces a script (i.e. input to it should be sanitized). */
    fun isRestricted(languageCode: String): Boolean =
        languageCode.lowercase() in allowedByLanguage

    /** Drops every letter that is outside [languageCode]'s script; non-letters always survive. */
    fun sanitize(languageCode: String, text: String): String {
        val allowed = allowedByLanguage[languageCode.lowercase()] ?: return text
        if (text.isEmpty()) return text

        val out = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val count = Character.charCount(cp)
            if (isAllowed(cp, allowed)) out.append(text, i, i + count)
            i += count
        }
        return out.toString()
    }

    private fun isAllowed(codePoint: Int, allowed: Set<UnicodeBlock>): Boolean =
        !Character.isLetter(codePoint) || UnicodeBlock.of(codePoint) in allowed
}
