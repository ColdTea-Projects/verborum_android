package de.coldtea.verborum.forum.marketplace.domain.model

/** How the browse list is sorted — each is its own endpoint (guide §5.1). */
enum class MarketplaceListingOrder {
    // GET /marketplace/dictionaries
    NEWEST,

    // GET /marketplace/dictionaries/popular: most imported first, newest first among equals.
    POPULAR,
}
