package de.coldtea.verborum.core.profile.domain.model

/**
 * The Forum (marketplace) terms the profile page shows. The backend stores whatever version the
 * client sends (guide §4.4), so bump [VERSION] whenever the terms text changes — a member whose
 * stored version differs can then be asked to accept the new text.
 */
object MarketplaceTerms {
    const val VERSION = "2026-10-04"
}
