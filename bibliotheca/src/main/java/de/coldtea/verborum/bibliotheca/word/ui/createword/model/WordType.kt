package de.coldtea.verborum.bibliotheca.word.ui.createword.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

enum class WordType(val metaType: String?, @StringRes val labelRes: Int) {
    NOUN("noun", ResStrings.createWordScreenTypeNoun),
    VERB("verb", ResStrings.createWordScreenTypeVerb),
    ADJECTIVE("adjective", ResStrings.createWordScreenTypeAdjective),
    FREE_TEXT(null, ResStrings.createWordScreenTypeFreeText),
}
