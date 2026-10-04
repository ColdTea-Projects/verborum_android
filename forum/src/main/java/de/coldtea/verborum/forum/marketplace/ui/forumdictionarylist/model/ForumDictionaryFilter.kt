package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter

/** The Forum search criteria; the default instance filters nothing. */
data class ForumDictionaryFilter(
    // In the order they were added; never two of the same pair.
    val languagePairs: List<ForumLanguagePair> = emptyList(),
    // Tag codes.
    val tags: Set<String> = emptySet(),
    // The publisher's user name; null when not filtering by user.
    val userName: String? = null,
) {
    val isActive: Boolean
        get() = this != ForumDictionaryFilter()

    fun convertToListingFilter() = MarketplaceListingFilter(
        languagePairs = languagePairs.map(ForumLanguagePair::convertToListingPair),
        tags = tags,
        publisherName = userName,
    )
}
