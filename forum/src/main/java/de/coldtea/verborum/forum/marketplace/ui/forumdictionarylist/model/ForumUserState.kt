package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

/**
 * How the signed-in user relates to the listings on screen: which are theirs, which they already
 * downloaded, which are downloading right now, and which turned out to be gone (404 on download).
 */
data class ForumUserState(
    val userId: String? = null,
    val downloadedIds: Set<String> = emptySet(),
    val downloadingIds: Set<String> = emptySet(),
    val unavailableIds: Set<String> = emptySet(),
) {
    fun applyTo(dictionary: ForumDictionaryUi) = dictionary.copy(
        isOwn = userId != null && dictionary.publisherId == userId,
        isDownloaded = dictionary.dictionaryId in downloadedIds,
        isDownloading = dictionary.dictionaryId in downloadingIds,
    )
}
