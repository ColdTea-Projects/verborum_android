package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import javax.inject.Inject

class GetMarketplaceListingApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    suspend operator fun invoke(dictionaryId: String): MarketplaceListing? =
        repository.getListing(dictionaryId)?.convertToListing()
}
