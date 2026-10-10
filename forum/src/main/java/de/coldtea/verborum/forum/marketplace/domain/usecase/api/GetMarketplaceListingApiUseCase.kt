package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.normaliseTags
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import retrofit2.HttpException
import javax.inject.Inject

class GetMarketplaceListingApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    /**
     * A shared dictionary for the details screen. Its name and languages are read fresh from
     * ms_dictionary; the publisher's name, import count, rating and publish date only exist on the
     * marketplace listing, so they come from the browse list's last load. Tags come from the
     * listing too, or are read fresh when it was not loaded this session.
     *
     * Null when the dictionary is gone — made private or deleted since the list loaded (404).
     */
    suspend operator fun invoke(dictionaryId: String): MarketplaceListing? {
        val dictionary = try {
            repository.getDictionary(dictionaryId)
        } catch (e: HttpException) {
            if (e.code() == HTTP_NOT_FOUND) return null
            throw e
        }
        val listing = repository.getLoadedListing(dictionaryId)

        return MarketplaceListing(
            dictionaryId = dictionary.dictionaryId,
            publisherId = dictionary.userId,
            publisherName = listing?.publisherName?.takeIf(String::isNotBlank),
            name = dictionary.name,
            fromLang = dictionary.fromLang,
            toLang = dictionary.toLang,
            importCount = listing?.importCount ?: 0,
            ratingAverage = listing?.ratingAverage,
            ratingCount = listing?.ratingCount ?: 0,
            publishedAt = ApiTimestamp.parse(listing?.publishedAt)
                ?: ApiTimestamp.parse(dictionary.createdAt)
                ?: 0L,
            tags = (listing?.tags ?: repository.getTags(dictionaryId).mapNotNull { it.tag }).normaliseTags(),
        )
    }

    private companion object {
        const val HTTP_NOT_FOUND = 404
    }
}
