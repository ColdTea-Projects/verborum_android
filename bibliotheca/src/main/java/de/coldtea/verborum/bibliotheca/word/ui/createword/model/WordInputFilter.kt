package de.coldtea.verborum.bibliotheca.word.ui.createword.model

/**
 * What may be typed into a word-form field, per `docs/word-input-filtering-android.md`.
 *
 * The rule is **letters of that side's language, and nothing else** (§3). Every non-letter that
 * appears in a stored surface — the space in *der Apfel*, the apostrophe in *l'eau* — comes from
 * article composition, which the app performs afterwards, so it never has to survive typing (§4).
 * Digits and punctuation appear in no surface in the data model and are rejected outright.
 *
 * Two fields sit outside the rule. Arabic `root` is stored with spaces between the letters
 * (`ك ت ب`), so it keeps them. And `reading` is unfiltered in every language: it is the user's own
 * pronunciation note — pinyin with tone marks, kana, or whatever transcription they find useful —
 * so they type it in their own keyboard and nothing is rejected. The chip fields (`aux`, `class`)
 * never reach this object because they carry no keyboard.
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

    /** Cap for typed words and grammatical forms: a word and its forms stay short. */
    const val MAX_TEXT_LENGTH = 40

    /** Cap for free text (the reading field and the free-text word type): room for a note. */
    const val FREE_TEXT_MAX_LENGTH = 150

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
     * True when [fieldKey] accepts anything at all. The reading is a free transcription the user
     * writes for themselves, so it takes no script and no letters-only rule — and, being typed in
     * the user's own keyboard rather than the dictionary language's, it would fight the filter
     * constantly if it did.
     */
    fun isUnfiltered(fieldKey: FieldKey?): Boolean = fieldKey == FieldKey.READING

    /**
     * Applies the rule for one field. [fieldKey] is null for the base word; a [FieldKey] for a
     * grammatical form. Free text passes through untouched — it is arbitrary content in any script
     * with any punctuation (§5). Every field is capped: [MAX_TEXT_LENGTH] for words and forms,
     * [FREE_TEXT_MAX_LENGTH] for free text.
     */
    fun apply(
        languageCode: String,
        wordType: WordType,
        fieldKey: FieldKey?,
        text: String,
    ): Filtered {
        val isFreeText = wordType == WordType.FREE_TEXT || isUnfiltered(fieldKey)
        val limited = truncate(text, if (isFreeText) FREE_TEXT_MAX_LENGTH else MAX_TEXT_LENGTH)

        if (isFreeText || limited.isEmpty()) {
            return Filtered(limited, null)
        }

        val code = languageCode.lowercase()
        val accepted = StringBuilder(limited.length)
        var droppedLetter = false
        var droppedOther = false

        var i = 0
        while (i < limited.length) {
            val codePoint = limited.codePointAt(i)
            val width = Character.charCount(codePoint)
            when {
                Character.isLetter(codePoint) ->
                    if (LanguageScript.allowsLetter(code, codePoint)) {
                        accepted.append(limited, i, i + width)
                    } else {
                        droppedLetter = true
                    }

                isAllowedNonLetter(codePoint, code, fieldKey) -> accepted.append(limited, i, i + width)

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

    /** Cuts [text] to [max] code points, never splitting a surrogate pair or combining mark. */
    private fun truncate(text: String, max: Int): String {
        if (text.length <= max) return text
        var i = 0
        var count = 0
        while (i < text.length && count < max) {
            i += Character.charCount(text.codePointAt(i))
            count++
        }
        return text.substring(0, i)
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
