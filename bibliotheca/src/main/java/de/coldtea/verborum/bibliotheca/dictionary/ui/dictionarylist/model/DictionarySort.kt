package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model

import androidx.annotation.StringRes
import de.coldtea.verborum.bibliotheca.common.utils.ResStrings

/**
 * How the dictionary list is ordered. [labelRes] is shown both in the sort bottom sheet and on the
 * sort chip. [NEWEST] is the default.
 *
 * The two language orders sort by the stored language *code* (a deterministic proxy for the
 * localized name, which the domain layer cannot resolve); a stable id tiebreaker in the view model
 * keeps equal rows from reshuffling.
 */
enum class DictionarySort(@StringRes val labelRes: Int) {
    NAME_ASC(ResStrings.dictionarySortNameAsc),
    NAME_DESC(ResStrings.dictionarySortNameDesc),
    FROM_LANGUAGE(ResStrings.dictionarySortFromLanguage),
    TO_LANGUAGE(ResStrings.dictionarySortToLanguage),
    NEWEST(ResStrings.dictionarySortNewest),
    OLDEST(ResStrings.dictionarySortOldest),
}
