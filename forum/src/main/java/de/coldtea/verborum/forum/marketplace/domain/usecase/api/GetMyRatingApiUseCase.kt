package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import retrofit2.HttpException
import javax.inject.Inject

/**
 * The user's own stars for a dictionary (guide §5.6): null on a 404, which is how the marketplace
 * says "not rated yet". Any other failure throws, so being offline never reads as "not rated".
 */
class GetMyRatingApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String): Int? {
        val response = repository.getRating(dictionaryId)
        return when {
            response.isSuccessful -> response.body()?.stars?.takeIf { it in MIN_STARS..MAX_STARS }
            response.code() == HTTP_NOT_FOUND -> null
            else -> throw HttpException(response)
        }
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
        const val MIN_STARS = 1
        const val MAX_STARS = 5
    }
}
