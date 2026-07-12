package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * Single source of truth for language-specific word-creation rules: which grammatical forms each
 * language exposes per word type, how gender renders on a chip, and how a gender + word compose
 * into a surface form (handling article elision such as French *l'eau* and Italian *lo studente*).
 *
 * Everything is keyed by the two-letter language code from
 * [de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage].
 */
object LanguageGrammar {

    private const val DE = "de"
    private const val FR = "fr"
    private const val ES = "es"
    private const val IT = "it"
    private const val PT = "pt"
    private const val NL = "nl"
    private const val LT = "lt"
    private const val TR = "tr"
    private const val AZ = "az"
    private const val EN = "en"

    /** Genders offered for nouns in each language. Absent = language has no grammatical gender. */
    private val genderByLanguage: Map<String, List<Gender>> = mapOf(
        DE to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        FR to listOf(Gender.MASCULINE, Gender.FEMININE),
        ES to listOf(Gender.MASCULINE, Gender.FEMININE),
        IT to listOf(Gender.MASCULINE, Gender.FEMININE),
        PT to listOf(Gender.MASCULINE, Gender.FEMININE),
        NL to listOf(Gender.COMMON, Gender.NEUTER),
        LT to listOf(Gender.MASCULINE, Gender.FEMININE),
    )

    /** Base definite article per gender, before elision. Absent = language uses no article. */
    private val articlesByLanguage: Map<String, Map<Gender, String>> = mapOf(
        DE to mapOf(Gender.MASCULINE to "der", Gender.FEMININE to "die", Gender.NEUTER to "das"),
        FR to mapOf(Gender.MASCULINE to "le", Gender.FEMININE to "la"),
        ES to mapOf(Gender.MASCULINE to "el", Gender.FEMININE to "la"),
        IT to mapOf(Gender.MASCULINE to "il", Gender.FEMININE to "la"),
        PT to mapOf(Gender.MASCULINE to "o", Gender.FEMININE to "a"),
        NL to mapOf(Gender.COMMON to "de", Gender.NEUTER to "het"),
    )

    /** Languages whose plural is irregular/unpredictable enough to be worth capturing. */
    private val pluralLanguages: Set<String> = genderByLanguage.keys + EN

    /** Languages whose adjectives inflect for a distinct feminine form. */
    private val feminineAdjectiveLanguages: Set<String> = setOf(FR, ES, IT, PT, LT)

    /**
     * Languages worth capturing comparative/superlative forms for (adjectives and adverbs).
     * Turkish/Azerbaijani are excluded: comparison there is fully periphrastic (daha…, ən…), so
     * there is nothing per-word to store.
     */
    private val comparisonLanguages: Set<String> = setOf(EN, DE, NL, FR, ES, IT, PT, LT)

    fun genderOptions(languageCode: String): List<Gender> =
        genderByLanguage[languageCode.lowercase()].orEmpty()

    /**
     * Builds the extra-input spec for one language side. Callers still render the base word field
     * unconditionally; this describes everything else.
     */
    fun formSpec(languageCode: String, wordType: WordType): LanguageFormSpec {
        val code = languageCode.lowercase()
        return when (wordType) {
            WordType.NOUN -> LanguageFormSpec(
                genderOptions = genderOptions(code),
                fields = buildList {
                    if (code in pluralLanguages) {
                        add(textForm(FieldKey.PLURAL, irregularHintOrNull(code)))
                    }
                },
            )

            WordType.VERB -> LanguageFormSpec(fields = verbFields(code))

            WordType.ADJECTIVE -> LanguageFormSpec(
                fields = buildList {
                    if (code in feminineAdjectiveLanguages) add(textForm(FieldKey.FEMININE))
                    addAll(comparisonFields(code))
                },
            )

            // Adverbs, free text, and the closed-class sub-types capture only the word itself.
            // (Adverb comparison overlaps almost entirely with the adjective card, and the few
            // adverb-only irregulars aren't worth a dedicated field.)
            else -> LanguageFormSpec()
        }
    }

    /**
     * Comparative + superlative forms, for languages where they are worth entering. Germanic/Baltic
     * inflect morphologically (always shown); the Romance languages and English are periphrastic, so
     * these are hinted as irregular-only. Empty for languages outside [comparisonLanguages].
     */
    private fun comparisonFields(code: String): List<FormField> =
        if (code in comparisonLanguages) {
            listOf(
                textForm(FieldKey.COMPARATIVE, comparisonHintOrNull(code)),
                textForm(FieldKey.SUPERLATIVE, comparisonHintOrNull(code)),
            )
        } else {
            emptyList()
        }

    private fun comparisonHintOrNull(code: String): Int? =
        // de/nl/lt comparison is morphological; en + Romance are periphrastic — capture only exceptions.
        if (code in setOf(EN, FR, ES, IT, PT)) ResStrings.createWordScreenIrregularHint else null

    private fun verbFields(code: String): List<FormField> = when (code) {
        EN -> listOf(
            textForm(FieldKey.PAST, irregularHintOrNull(code)),
            textForm(FieldKey.PARTICIPLE, irregularHintOrNull(code)),
        )

        DE -> listOf(
            textForm(FieldKey.PAST),
            textForm(FieldKey.PARTICIPLE),
            FormField.ChoiceForm(FieldKey.AUXILIARY, listOf("haben", "sein")),
        )

        NL -> listOf(
            textForm(FieldKey.PAST),
            textForm(FieldKey.PARTICIPLE),
            FormField.ChoiceForm(FieldKey.AUXILIARY, listOf("hebben", "zijn")),
        )

        FR -> listOf(
            textForm(FieldKey.PARTICIPLE),
            FormField.ChoiceForm(FieldKey.AUXILIARY, listOf("avoir", "être")),
        )

        IT -> listOf(
            textForm(FieldKey.PARTICIPLE),
            FormField.ChoiceForm(FieldKey.AUXILIARY, listOf("avere", "essere")),
        )

        ES, PT -> listOf(
            textForm(FieldKey.PARTICIPLE, irregularHintOrNull(code)),
        )

        LT -> listOf(
            textForm(FieldKey.PRESENT_3RD),
            textForm(FieldKey.PAST_3RD),
        )

        else -> emptyList() // tr, az: regular conjugation, nothing to capture
    }

    private fun textForm(key: FieldKey, hintRes: Int? = null) =
        FormField.TextForm(key = key, hintRes = hintRes)

    private fun irregularHintOrNull(code: String): Int? =
        // English/Spanish/Portuguese default forms are regular; only capture the exceptions.
        if (code == EN || code == ES || code == PT) ResStrings.createWordScreenIrregularHint else null

    /** What the gender chip displays: the article where one exists, otherwise a grammatical label. */
    fun genderLabel(languageCode: String, gender: Gender): GenderLabel {
        val article = articlesByLanguage[languageCode.lowercase()]?.get(gender)
        if (article != null) return GenderLabel.Article(article)
        return GenderLabel.Localized(
            when (gender) {
                Gender.MASCULINE -> ResStrings.createWordScreenGenderMasculine
                Gender.FEMININE -> ResStrings.createWordScreenGenderFeminine
                Gender.NEUTER -> ResStrings.createWordScreenGenderNeuter
                Gender.COMMON -> ResStrings.createWordScreenGenderCommon
            }
        )
    }

    /**
     * Composes a gender + base word into its dictionary surface form, applying the language's
     * article and elision rules. Returns the bare word when the language/gender has no article
     * (Lithuanian, Turkish, free text) — gender is still preserved separately in the meta.
     */
    fun composeSurface(languageCode: String, gender: Gender?, word: String): String {
        val trimmed = word.trim()
        if (trimmed.isEmpty() || gender == null) return trimmed
        val code = languageCode.lowercase()
        val article = articlesByLanguage[code]?.get(gender) ?: return trimmed

        return when (code) {
            FR -> if (startsWithVowelSound(trimmed)) "l'$trimmed" else "$article $trimmed"
            IT -> "${italianArticle(gender, trimmed)} $trimmed".let(::collapseElision)
            else -> "$article $trimmed"
        }
    }

    // "l'" attaches with no following space; every other article is space-separated.
    private fun collapseElision(text: String): String = text.replace("' ", "'")

    /**
     * Inverse of [composeSurface]: strips the language's article from a stored surface form so it
     * can be edited ("der Apfel" → "Apfel", "l'eau" → "eau", "lo studente" → "studente").
     * Returns the input untouched when no known article prefix matches.
     */
    fun extractBaseWord(languageCode: String, surface: String): String {
        val code = languageCode.lowercase()
        val trimmed = surface.trim()

        if ((code == FR || code == IT) && trimmed.startsWith("l'")) {
            return trimmed.removePrefix("l'")
        }

        val articles = articlesByLanguage[code]?.values.orEmpty() +
            listOfNotNull("lo".takeIf { code == IT })
        articles.forEach { article ->
            if (trimmed.startsWith("$article ")) return trimmed.removePrefix("$article ")
        }

        return trimmed
    }

    private fun italianArticle(gender: Gender, word: String): String = when (gender) {
        Gender.FEMININE -> if (startsWithVowelSound(word)) "l'" else "la"
        else -> when {
            startsWithVowelSound(word) -> "l'"
            requiresItalianLo(word) -> "lo"
            else -> "il"
        }
    }

    private fun startsWithVowelSound(word: String): Boolean {
        val first = word.firstOrNull()?.lowercaseChar() ?: return false
        // French/Italian both elide before a vowel; French also before a (mostly mute) h.
        return first in "aeiouàâäéèêëíìîïóòôöúùûü" || first == 'h'
    }

    private fun requiresItalianLo(word: String): Boolean {
        val lower = word.lowercase()
        val second = lower.getOrNull(1)
        return when (lower.firstOrNull()) {
            'z', 'x', 'y' -> true
            'g' -> second == 'n'
            'p' -> second == 's' || second == 'n'
            's' -> second != null && second !in "aeiouàâäéèêëíìîïóòôöúùûü"
            else -> false
        }
    }
}
