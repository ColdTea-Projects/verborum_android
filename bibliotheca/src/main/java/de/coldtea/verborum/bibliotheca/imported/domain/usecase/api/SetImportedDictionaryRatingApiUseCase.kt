package de.coldtea.verborum.bibliotheca.imported.domain.usecase.api

import de.coldtea.verborum.core.library.domain.MarketplaceRatings
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import javax.inject.Inject

class SetImportedDictionaryRatingApiUseCase @Inject constructor(
    private val marketplaceRatings: MarketplaceRatings,
) {
    suspend fun invoke(dictionaryId: String, stars: Int): RatingOutcome =
        marketplaceRatings.setRating(dictionaryId, stars)
}
