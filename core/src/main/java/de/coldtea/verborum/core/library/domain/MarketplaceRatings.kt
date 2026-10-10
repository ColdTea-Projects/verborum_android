package de.coldtea.verborum.core.library.domain

import de.coldtea.verborum.core.library.domain.model.RatingOutcome

/**
 * The user's own 1–5 star rating of a dictionary they downloaded, kept by the marketplace
 * (guide §5.6, §6.10). Online-only, like import — nothing is queued offline.
 *
 * The downloaded dictionary's details screen lives in bibliotheca, the marketplace client in the
 * Forum, and neither may depend on the other — so the Forum binds the implementation and
 * bibliotheca talks to it through this.
 */
interface MarketplaceRatings {

    /** The user's stars (1–5), or null when they have not rated it. Throws when it cannot be read. */
    suspend fun getMyRating(dictionaryId: String): Int?

    suspend fun setRating(dictionaryId: String, stars: Int): RatingOutcome

    suspend fun removeRating(dictionaryId: String): RatingOutcome
}
