package de.coldtea.verborum.bibliotheca.dictionary.domain.model

/**
 * The user's Forum sharing as the rules need it (marketplace guide §4.6): whether they are a
 * member, and which of their own dictionaries are shared.
 */
data class SharingState(
    val isMember: Boolean = false,
    val sharedDictionaryIds: Set<String> = emptySet(),
) {
    /**
     * A member must keep at least one dictionary shared, so their only shared one can be neither
     * unshared nor deleted while they stay in the Forum.
     */
    fun isLastShared(dictionaryId: String): Boolean =
        isMember && sharedDictionaryIds == setOf(dictionaryId)
}
