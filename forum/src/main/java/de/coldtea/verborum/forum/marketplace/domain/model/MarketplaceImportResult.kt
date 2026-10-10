package de.coldtea.verborum.forum.marketplace.domain.model

/** How a download (import) ended (`docs/marketplace-client-guide.md` §5.3). */
sealed interface MarketplaceImportResult {
    /** Recorded — also on a repeat; the local copy follows. */
    data object Imported : MarketplaceImportResult

    /** Made private, deleted, or its publisher left the Forum since the list loaded (404). */
    data object Unavailable : MarketplaceImportResult

    /** The caller is not (or no longer) a Forum member (403). */
    data object NotMember : MarketplaceImportResult

    /** Anything else — offline, or a server error after the one retry. */
    data object Failed : MarketplaceImportResult
}
