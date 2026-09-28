package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import javax.inject.Inject

class GetMarketplaceListingsApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    /** The first page, newest first; de-duplicated by id as the guide requires when paging (§4.1). */
    suspend operator fun invoke(): List<MarketplaceListing> =
        repository.getListings().items
            .distinctBy { it.dictionaryId }
            .map(MarketplaceListingResponse::convertToListing)
}
