package de.coldtea.verborum.forum.marketplace.domain.model

/**
 * What the browse list is narrowed to; an empty/null field does not filter. Tag codes are the
 * lowercase taxonomy codes.
 */
data class MarketplaceListingFilter(
    // A listing must be in one of these pairs, either direction (DE–TR matches DE→TR and TR→DE).
    val languagePairs: List<MarketplaceLanguagePair> = emptyList(),
    // A listing must carry at least one of these.
    val tags: Set<String> = emptySet(),
    // Matched case-insensitively against part of the publisher's name.
    val publisherName: String? = null,
)
