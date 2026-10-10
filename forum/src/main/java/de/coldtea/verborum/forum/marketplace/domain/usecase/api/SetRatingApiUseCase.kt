package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * `PUT …/{id}/rating` (guide §5.6). The status maps to a [RatingOutcome]: 403 (not a member, or
 * not downloaded) and 400 (the user's own dictionary, bad stars) are refusals, 404 means it is no
 * longer listed. Idempotent, so repeating the same stars changes nothing.
 */
class SetRatingApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String, stars: Int): RatingOutcome {
        require(stars in 1..5) { "stars must be 1..5, was $stars" }
        return try {
            repository.setRating(dictionaryId, stars).toOutcome()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RatingOutcome.FAILED
        }
    }
}

/** Shared by set and remove: the guide's status table for the rating endpoints. */
internal fun retrofit2.Response<*>.toOutcome(): RatingOutcome = when (code()) {
    in 200..299 -> RatingOutcome.DONE
    404 -> RatingOutcome.UNAVAILABLE
    400, 403 -> RatingOutcome.NOT_ALLOWED
    else -> RatingOutcome.FAILED
}
