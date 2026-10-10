package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/** `DELETE …/{id}/rating` (guide §5.6): 200 also when the user had no rating. */
class RemoveRatingApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String): RatingOutcome =
        try {
            repository.removeRating(dictionaryId).toOutcome()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RatingOutcome.FAILED
        }
}
