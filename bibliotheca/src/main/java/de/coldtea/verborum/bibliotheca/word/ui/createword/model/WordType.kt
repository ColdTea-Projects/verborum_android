package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * The part of speech stored in the word meta. Nouns/verbs/adjectives/adverbs carry their own
 * grammatical forms; the rest share the "Other" bucket and store nothing but their [metaType]
 * (free text keeps [metaType] = null, so words saved before sub-types load back as free text).
 */
enum class WordType(
    val metaType: String?,
    @StringRes val labelRes: Int,
    val category: WordCategory,
) {
    NOUN("noun", ResStrings.createWordScreenTypeNoun, WordCategory.NOUN),
    VERB("verb", ResStrings.createWordScreenTypeVerb, WordCategory.VERB),
    ADJECTIVE("adjective", ResStrings.createWordScreenTypeAdjective, WordCategory.ADJECTIVE),
    ADVERB("adverb", ResStrings.createWordScreenTypeAdverb, WordCategory.ADVERB),
    FREE_TEXT(null, ResStrings.createWordScreenTypeFreeText, WordCategory.OTHER),
    PREPOSITION("preposition", ResStrings.createWordScreenTypePreposition, WordCategory.OTHER),
    PRONOUN("pronoun", ResStrings.createWordScreenTypePronoun, WordCategory.OTHER),
    NUMERAL("numeral", ResStrings.createWordScreenTypeNumeral, WordCategory.OTHER),
    CONJUNCTION("conjunction", ResStrings.createWordScreenTypeConjunction, WordCategory.OTHER),
    INTERJECTION("interjection", ResStrings.createWordScreenTypeInterjection, WordCategory.OTHER),
    ARTICLE("article", ResStrings.createWordScreenTypeArticle, WordCategory.OTHER);

    companion object {
        /** Sub-types offered under the "Other" category, in dropdown order (free text first). */
        val otherTypes: List<WordType> = entries.filter { it.category == WordCategory.OTHER }
    }
}

/**
 * Top-level word-type chip shown on the create screen. All the closed-class parts of speech collapse
 * into [OTHER], which then offers a sub-type dropdown; the others map to a single [WordType].
 */
enum class WordCategory(@StringRes val labelRes: Int) {
    NOUN(ResStrings.createWordScreenTypeNoun),
    VERB(ResStrings.createWordScreenTypeVerb),
    ADJECTIVE(ResStrings.createWordScreenTypeAdjective),
    ADVERB(ResStrings.createWordScreenTypeAdverb),
    OTHER(ResStrings.createWordScreenTypeOther),
}

/** The word type a freshly-picked category starts on ("Other" defaults to free text). */
val WordCategory.defaultType: WordType
    get() = when (this) {
        WordCategory.NOUN -> WordType.NOUN
        WordCategory.VERB -> WordType.VERB
        WordCategory.ADJECTIVE -> WordType.ADJECTIVE
        WordCategory.ADVERB -> WordType.ADVERB
        WordCategory.OTHER -> WordType.FREE_TEXT
    }
