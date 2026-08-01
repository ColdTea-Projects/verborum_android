package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * What may be typed into a word-form field, per `docs/word-input-filtering-android.md`.
 *
 * The rule is **letters of that side's language, and nothing else** (§3). Every non-letter that
 * appears in a stored surface — the space in *der Apfel*, the apostrophe in *l'eau* — comes from
 * article composition, which the app performs afterwards, so it never has to survive typing (§4).
 * Digits and punctuation appear in no surface in the data model and are rejected outright.
 *
 * Of the two field-level exceptions in §7, only one needs a rule here: Arabic `root` is stored with
 * spaces between the letters (`ك ت ب`). Chinese `reading` needs none — a pinyin tone mark is either
 * a precomposed Latin letter (`ū`) or a letter plus a combining mark, and Latin is already this
 * side's composition alphabet, so `shū`/`mǎi` pass without a special case. The chip fields (`aux`,
 * `class`) never reach this object because they carry no keyboard.
 *
 * Filtering the field is the only lever available — the on-screen keyboard belongs to the user's
 * IME and cannot be altered (§2). Note the mirrored-contract risk in §8: the webapp's
 * `KeyboardLayout` implements the same rule in separate code, and the two must not drift.
 */
object WordInputFilter {

    /** Why a change was altered, so the field can explain itself. */
    enum class Rejection {
        /** A letter from another script (Cyrillic typed into a Greek field). */
        FOREIGN_SCRIPT,

        /** A digit, punctuation mark, symbol, or space that this field does not accept. */
        NON_LETTER,
    }

    /** The accepted text plus the reason it differs from the input, if it does. */
    data class Filtered(val text: String, val rejection: Rejection?)

    private const val AR = "ar"
    private const val ZH = "zh"

    /**
     * Languages whose IME builds candidate text inside the field before the user commits it. A
     * per-keystroke filter corrupts that intermediate text, so callers must hold these back until
     * composition ends (§6).
     */
    private val composingImeLanguages = setOf("ja", ZH, "ko")

    fun defersToCommit(languageCode: String): Boolean =
        languageCode.lowercase() in composingImeLanguages

    /**
     * Applies the rule for one field. [fieldKey] is null for the base word; a [FieldKey] for a
     * grammatical form. Free text passes through untouched — it is arbitrary content in any script
     * with any punctuation (§5).
     */
    fun apply(
        languageCode: String,
        wordType: WordType,
        fieldKey: FieldKey?,
        text: String,
    ): Filtered {
        if (wordType == WordType.FREE_TEXT || text.isEmpty()) return Filtered(text, null)

        val code = languageCode.lowercase()
        val accepted = StringBuilder(text.length)
        var droppedLetter = false
        var droppedOther = false

        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            val width = Character.charCount(codePoint)
            when {
                Character.isLetter(codePoint) ->
                    if (LanguageScript.allowsLetter(code, codePoint)) {
                        accepted.append(text, i, i + width)
                    } else {
                        droppedLetter = true
                    }

                isAllowedNonLetter(codePoint, code, fieldKey) -> accepted.append(text, i, i + width)

                else -> droppedOther = true
            }
            i += width
        }

        // A foreign letter is the more useful thing to report: it names a language, where "letters
        // only" does not. Punctuation is only surfaced when nothing worse happened.
        val rejection = when {
            droppedLetter -> Rejection.FOREIGN_SCRIPT
            droppedOther -> Rejection.NON_LETTER
            else -> null
        }
        return Filtered(accepted.toString(), rejection)
    }

    private fun isAllowedNonLetter(codePoint: Int, code: String, fieldKey: FieldKey?): Boolean =
        when {
            // Combining marks are half of a letter, not punctuation: a decomposed ü or a pinyin
            // tone mark arrives as letter + mark, and dropping the mark mangles the word.
            isCombiningMark(codePoint) -> true
            codePoint == ' '.code -> allowsSpace(code, fieldKey)
            else -> false
        }

    private fun isCombiningMark(codePoint: Int): Boolean = when (Character.getType(codePoint)) {
        Character.NON_SPACING_MARK.toInt(), Character.COMBINING_SPACING_MARK.toInt() -> true
        else -> false
    }

    // The Arabic consonantal root is stored letter-spaced (ك ت ب), the one typed field where a
    // space is part of the value rather than something composition adds later (§7).
    private fun allowsSpace(code: String, fieldKey: FieldKey?): Boolean =
        code == AR && fieldKey == FieldKey.ROOT
}
