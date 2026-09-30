package de.coldtea.verborum.forum.marketplace.domain.model

/** One page of the marketplace browse list; [hasMore] is false on the last page. */
data class MarketplaceListingPage(
    val listings: List<MarketplaceListing>,
    val page: Int,
    val hasMore: Boolean,
)
