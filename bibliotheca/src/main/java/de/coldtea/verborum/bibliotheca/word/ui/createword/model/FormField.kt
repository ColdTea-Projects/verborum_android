package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import androidx.annotation.StringRes

/**
 * Declarative description of the extra input controls a [LanguageInputCard] should render for a
 * given (language, word type) pair, beyond the always-present base word field.
 *
 * [genderOptions] drives the gender/article chip row (empty = no gender selector). [fields] are
 * the remaining controls, rendered in list order.
 */
data class LanguageFormSpec(
    val genderOptions: List<Gender> = emptyList(),
    val fields: List<FormField> = emptyList(),
) {
    val isEmpty: Boolean get() = genderOptions.isEmpty() && fields.isEmpty()
}

sealed interface FormField {
    val key: FieldKey

    /** A single-line free-text form (plural, participle, feminine…). */
    data class TextForm(
        override val key: FieldKey,
        @StringRes val labelRes: Int = key.labelRes,
        @StringRes val hintRes: Int? = null,
    ) : FormField

    /** A pick-one control over literal words that are the same in any UI language (e.g. haben/sein). */
    data class ChoiceForm(
        override val key: FieldKey,
        val options: List<String>,
        @StringRes val labelRes: Int = key.labelRes,
    ) : FormField

    /**
     * A pick-one control whose options store a stable ASCII [Option.code] but show a localized
     * [Option.labelRes] on the chip (e.g. Japanese verb class: code `group1`, label "Godan (う)").
     * Unlike [ChoiceForm], the stored value and the displayed text differ.
     */
    data class LabeledChoiceForm(
        override val key: FieldKey,
        val options: List<Option>,
        @StringRes val labelRes: Int = key.labelRes,
    ) : FormField {
        data class Option(val code: String, @StringRes val labelRes: Int)
    }
}

/** How a gender is shown on its selector chip: either a literal article or a localized label. */
sealed interface GenderLabel {
    data class Article(val text: String) : GenderLabel
    data class Localized(@StringRes val res: Int) : GenderLabel
}
