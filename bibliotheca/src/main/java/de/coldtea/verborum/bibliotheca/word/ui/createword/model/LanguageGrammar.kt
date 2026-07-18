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
    private const val PL = "pl"
    private const val UK = "uk"
    private const val RU = "ru"
    private const val EL = "el"
    private const val AR = "ar"
    private const val FA = "fa"
    private const val JA = "ja"
    private const val ZH = "zh"
    private const val KO = "ko"

    /** Genders offered for nouns in each language. Absent = language has no grammatical gender. */
    private val genderByLanguage: Map<String, List<Gender>> = mapOf(
        DE to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        FR to listOf(Gender.MASCULINE, Gender.FEMININE),
        ES to listOf(Gender.MASCULINE, Gender.FEMININE),
        IT to listOf(Gender.MASCULINE, Gender.FEMININE),
        PT to listOf(Gender.MASCULINE, Gender.FEMININE),
        NL to listOf(Gender.COMMON, Gender.NEUTER),
        LT to listOf(Gender.MASCULINE, Gender.FEMININE),
        // Slavic + Greek: three genders shown as labels (Greek adds articles below); Arabic: two.
        PL to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        UK to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        RU to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        EL to listOf(Gender.MASCULINE, Gender.FEMININE, Gender.NEUTER),
        AR to listOf(Gender.MASCULINE, Gender.FEMININE),
    )

    /** Base definite article per gender, before elision. Absent = language uses no article. */
    private val articlesByLanguage: Map<String, Map<Gender, String>> = mapOf(
        DE to mapOf(Gender.MASCULINE to "der", Gender.FEMININE to "die", Gender.NEUTER to "das"),
        FR to mapOf(Gender.MASCULINE to "le", Gender.FEMININE to "la"),
        ES to mapOf(Gender.MASCULINE to "el", Gender.FEMININE to "la"),
        IT to mapOf(Gender.MASCULINE to "il", Gender.FEMININE to "la"),
        PT to mapOf(Gender.MASCULINE to "o", Gender.FEMININE to "a"),
        NL to mapOf(Gender.COMMON to "de", Gender.NEUTER to "het"),
        EL to mapOf(Gender.MASCULINE to "ο", Gender.FEMININE to "η", Gender.NEUTER to "το"),
    )

    /**
     * Languages whose plural is irregular/unpredictable enough to be worth capturing. Farsi joins
     * the gendered languages + English: its regular ‑ها needs no entry, but borrowed Arabic broken
     * plurals (کتاب → کتب) do — captured as an irregular-only hint.
     */
    private val pluralLanguages: Set<String> = genderByLanguage.keys + EN + FA

    /** Languages that record a kana/pinyin reading on every typed word (leads all other forms). */
    private val readingLanguages: Set<String> = setOf(JA, ZH)

    /** Languages whose nouns and verbs carry a consonantal root (Arabic). */
    private val rootLanguages: Set<String> = setOf(AR)

    /** Languages whose nouns carry a measure word / classifier (Chinese). */
    private val measureLanguages: Set<String> = setOf(ZH)

    /** Languages whose adjectives inflect for a distinct feminine form. */
    private val feminineAdjectiveLanguages: Set<String> = setOf(FR, ES, IT, PT, LT, AR)

    /**
     * Languages worth capturing comparative/superlative forms for (adjectives and adverbs).
     * Turkish/Azerbaijani are excluded: comparison there is fully periphrastic (daha…, ən…), so
     * there is nothing per-word to store. Slavic + Greek join (morphological, or irregular-only).
     */
    private val comparisonLanguages: Set<String> = setOf(EN, DE, NL, FR, ES, IT, PT, LT, PL, UK, RU, EL)

    fun genderOptions(languageCode: String): List<Gender> =
        genderByLanguage[languageCode.lowercase()].orEmpty()

    /**
     * Builds the extra-input spec for one language side. Callers still render the base word field
     * unconditionally; this describes everything else.
     */
    fun formSpec(languageCode: String, wordType: WordType): LanguageFormSpec {
        val code = languageCode.lowercase()
        return LanguageFormSpec(
            genderOptions = if (wordType == WordType.NOUN) genderOptions(code) else emptyList(),
            fields = buildList {
                // Reading (kana/pinyin) leads every typed entry for the languages that use it —
                // including adverbs and the closed-class sub-types, but not untyped free text.
                if (code in readingLanguages && wordType != WordType.FREE_TEXT) {
                    add(textForm(FieldKey.READING))
                }
                addAll(typeFields(code, wordType))
            },
        )
    }

    /** The forms specific to a word type, beyond gender and the reading handled by [formSpec]. */
    private fun typeFields(code: String, wordType: WordType): List<FormField> = when (wordType) {
        WordType.NOUN -> buildList {
            if (code in pluralLanguages) add(textForm(FieldKey.PLURAL, irregularHintOrNull(code)))
            if (code in rootLanguages) add(textForm(FieldKey.ROOT))
            if (code in measureLanguages) add(textForm(FieldKey.MEASURE))
        }

        WordType.VERB -> verbFields(code)

        WordType.ADJECTIVE -> buildList {
            if (code in feminineAdjectiveLanguages) add(textForm(FieldKey.FEMININE, feminineHintOrNull(code)))
            addAll(comparisonFields(code))
            when (code) {
                JA -> add(japaneseAdjectiveClass())
                KO -> add(textForm(FieldKey.POLITE))
            }
        }

        // Adverbs, free text, and the closed-class sub-types capture only the word itself (plus a
        // reading where the language uses one). Adverb comparison overlaps with the adjective card,
        // and the few adverb-only irregulars aren't worth a dedicated field.
        else -> emptyList()
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
        // de/nl/lt/pl/uk comparison is morphological; en + Romance + ru + el are periphrastic or
        // regular-by-default, so capture only the exceptions.
        if (code in setOf(EN, FR, ES, IT, PT, RU, EL)) ResStrings.createWordScreenIrregularHint else null

    private fun verbFields(code: String): List<FormField> = when (code) {
        EN -> listOf(
            textForm(FieldKey.PAST, irregularHintOrNull(code)),
            textForm(FieldKey.PARTICIPLE, irregularHintOrNull(code)),
        )

        // Slavic present (3sg) + aspect counterpart of the imperfective.
        PL, UK, RU -> listOf(
            textForm(FieldKey.PRESENT_3RD),
            textForm(FieldKey.ASPECT),
        )

        EL -> listOf(textForm(FieldKey.PAST)) // aorist

        AR -> listOf(
            textForm(FieldKey.PRESENT_3RD),
            textForm(FieldKey.ROOT),
        )

        FA -> listOf(textForm(FieldKey.STEM)) // present stem, the irregular core of Farsi verbs

        JA -> listOf(japaneseVerbClass()) // reading is prepended by formSpec

        KO -> listOf(textForm(FieldKey.POLITE)) // 해요체; verbs and adjectives conjugate alike

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

        // tr, az: regular conjugation; zh: reading only (added by formSpec) — nothing more here.
        else -> emptyList()
    }

    /** Japanese verb conjugation class: stored as a stable code, shown as a localized chip. */
    private fun japaneseVerbClass(): FormField.LabeledChoiceForm = FormField.LabeledChoiceForm(
        key = FieldKey.CLASS,
        options = listOf(
            FormField.LabeledChoiceForm.Option("group1", ResStrings.createWordScreenClassGroup1),
            FormField.LabeledChoiceForm.Option("group2", ResStrings.createWordScreenClassGroup2),
            FormField.LabeledChoiceForm.Option("irregular", ResStrings.createWordScreenClassIrregular),
        ),
    )

    /** Japanese adjective class: い-adjective vs な-adjective. */
    private fun japaneseAdjectiveClass(): FormField.LabeledChoiceForm = FormField.LabeledChoiceForm(
        key = FieldKey.CLASS,
        options = listOf(
            FormField.LabeledChoiceForm.Option("i", ResStrings.createWordScreenClassIAdjective),
            FormField.LabeledChoiceForm.Option("na", ResStrings.createWordScreenClassNaAdjective),
        ),
    )

    private fun textForm(key: FieldKey, hintRes: Int? = null) =
        FormField.TextForm(key = key, hintRes = hintRes)

    private fun irregularHintOrNull(code: String): Int? =
        // English/Spanish/Portuguese default forms are regular, as is Farsi's ‑ها plural; capture
        // only the exceptions.
        if (code == EN || code == ES || code == PT || code == FA) ResStrings.createWordScreenIrregularHint else null

    private fun feminineHintOrNull(code: String): Int? =
        // Arabic's feminine ة is the regular default; only its irregular feminines are worth entering.
        if (code == AR) ResStrings.createWordScreenIrregularHint else null

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
