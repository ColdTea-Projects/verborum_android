package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import java.lang.Character.UnicodeBlock

/**
 * Which script each dictionary language writes in: a Greek side accepts only Greek letters, an
 * Arabic side only Arabic letters, and so on. This object rules on **letters only** — it is the
 * script half of the input contract; [WordInputFilter] owns the rest (which non-letters, if any,
 * a given field accepts) and is what the UI calls.
 *
 * Japanese and Chinese are special: their keyboards type romaji/pinyin (Latin) that the IME
 * converts to kana/hanzi, so Latin is permitted there as the composition alphabet — other foreign
 * scripts are still rejected. Unknown language codes are left unrestricted.
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

    /** True when the language enforces a script (i.e. its letters are constrained). */
    fun isRestricted(languageCode: String): Boolean =
        languageCode.lowercase() in allowedByLanguage

    /**
     * True when [codePoint] is a letter [languageCode] is written with. Anything that is not a
     * letter is not this object's business and always passes; an unknown language code accepts
     * every letter.
     */
    fun allowsLetter(languageCode: String, codePoint: Int): Boolean {
        if (!Character.isLetter(codePoint)) return true
        val allowed = allowedByLanguage[languageCode.lowercase()] ?: return true
        // Unassigned code points have no block; nothing is written with them, so reject.
        val block = UnicodeBlock.of(codePoint) ?: return false
        return block in allowed
    }
}
