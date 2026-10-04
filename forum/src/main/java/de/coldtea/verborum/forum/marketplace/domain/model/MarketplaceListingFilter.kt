package de.coldtea.verborum.forum.marketplace.domain.model

/**
 * What the browse list is narrowed to; an empty/null field does not filter. Tag codes are the
 * lowercase taxonomy codes. The backend's limits are below — exceeding one is a 400 (guide §5.1).
 */
data class MarketplaceListingFilter(
    // A listing must be in one of these pairs, either direction (DE–TR matches DE→TR and TR→DE).
    val languagePairs: List<MarketplaceLanguagePair> = emptyList(),
    // A listing must carry at least one of these.
    val tags: Set<String> = emptySet(),
    // Matched case-insensitively against part of the publisher's name.
    val publisherName: String? = null,
) {
    companion object {
        /** At most this many pairs, and this many tags, per query. */
        const val MAX_VALUES_PER_FILTER = 10

        const val MIN_PUBLISHER_NAME_LENGTH = 3
        const val MAX_PUBLISHER_NAME_LENGTH = 255
    }
}
