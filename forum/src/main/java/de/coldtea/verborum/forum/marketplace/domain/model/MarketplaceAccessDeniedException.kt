package de.coldtea.verborum.forum.marketplace.domain.model

/**
 * The marketplace answered 403: the caller is not (or no longer) a Forum member — they left on
 * another device, or the join has not reached the marketplace yet (guide §4.6, §6.3).
 */
class MarketplaceAccessDeniedException : Exception("Not a Forum member")
