package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model

import de.coldtea.verborum.bibliotheca.common.utils.ResStrings
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.core.ui.UiText

/**
 * What to tell the user after a delete; null when the outcome speaks for itself (the dictionary is
 * gone, or will be once sync reaches the server). Shared by the list and the details screen.
 */
fun deletionMessage(deletion: DictionaryDeletion): UiText? = when (deletion) {
    DictionaryDeletion.DELETED, DictionaryDeletion.PENDING -> null
    DictionaryDeletion.SHARING_REQUIRED -> UiText.Resource(ResStrings.errorDeleteLastShared)
    DictionaryDeletion.REFUSED -> UiText.Resource(ResStrings.errorDeleteFailed)
}
