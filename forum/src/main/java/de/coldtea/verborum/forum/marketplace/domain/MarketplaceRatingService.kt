package de.coldtea.verborum.forum.marketplace.domain

import de.coldtea.verborum.core.library.domain.MarketplaceRatings
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMyRatingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.RemoveRatingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.SetRatingApiUseCase
import javax.inject.Inject

/**
 * The user's own star rating of a downloaded dictionary (guide §5.6). Used by the Forum's details
 * screen directly, and by bibliotheca's through core's [MarketplaceRatings]. Online-only.
 */
class MarketplaceRatingService @Inject constructor(
    private val getMyRatingApiUseCase: GetMyRatingApiUseCase,
    private val setRatingApiUseCase: SetRatingApiUseCase,
    private val removeRatingApiUseCase: RemoveRatingApiUseCase,
) : MarketplaceRatings {

    override suspend fun getMyRating(dictionaryId: String): Int? = getMyRatingApiUseCase(dictionaryId)

    override suspend fun setRating(dictionaryId: String, stars: Int): RatingOutcome =
        setRatingApiUseCase(dictionaryId, stars)

    override suspend fun removeRating(dictionaryId: String): RatingOutcome = removeRatingApiUseCase(dictionaryId)
}
