package de.coldtea.verborum.forum.marketplace.domain.model

/**
 * What the browse list is narrowed to; a null/empty field does not filter. Language codes are the
 * wire's uppercase codes, tag codes the lowercase taxonomy codes.
 */
data class MarketplaceListingFilter(
    val fromLang: String? = null,
    val toLang: String? = null,
    // A listing must carry at least one of these.
    val tags: Set<String> = emptySet(),
    // Matched case-insensitively against part of the publisher's name.
    val publisherName: String? = null,
)
