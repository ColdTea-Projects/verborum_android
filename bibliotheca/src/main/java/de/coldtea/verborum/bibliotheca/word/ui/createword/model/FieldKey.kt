package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * A grammatical form captured alongside the base word, beyond gender and the word itself.
 * [metaKey] is the key written into the word meta string (e.g. `plural=Äpfel`); [labelRes] is the
 * user-facing name of the form, shared by the create-word fields and the quiz screens.
 *
 * The declaration order defines the order these are serialized into the meta string.
 */
enum class FieldKey(val metaKey: String, @StringRes val labelRes: Int) {
    PLURAL("plural", ResStrings.createWordScreenPluralLabel),
    FEMININE("feminine", ResStrings.createWordScreenFeminineLabel),
    PRESENT_3RD("present", ResStrings.createWordScreenPresentLabel),
    PAST("past", ResStrings.createWordScreenPastLabel),
    PAST_3RD("past3", ResStrings.createWordScreenPastFormLabel),
    PARTICIPLE("participle", ResStrings.createWordScreenParticipleLabel),
    AUXILIARY("aux", ResStrings.createWordScreenAuxiliaryLabel),
}
