package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.core.library.domain.MarketplaceRatings
import javax.inject.Inject

/** The user's own stars for a downloaded dictionary; null when not rated yet. Throws offline. */
class GetImportedDictionaryRatingApiUseCase @Inject constructor(
    private val marketplaceRatings: MarketplaceRatings,
) {
    suspend fun invoke(dictionaryId: String): Int? = marketplaceRatings.getMyRating(dictionaryId)
}
