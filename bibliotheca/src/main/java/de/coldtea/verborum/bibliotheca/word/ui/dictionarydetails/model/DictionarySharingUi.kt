package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model

/**
 * The "Shared in the Forum" switch of one of a member's own dictionaries. [isLastShared]: it is
 * their only shared one, so it can be neither unshared nor deleted while they stay in the Forum.
 */
data class DictionarySharingUi(
    val isShared: Boolean,
    val isLastShared: Boolean,
    // A change is on its way to the server; the switch waits for the answer.
    val isUpdating: Boolean = false,
) {
    val canToggle: Boolean get() = !isUpdating && !(isShared && isLastShared)
}
