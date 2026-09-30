package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import javax.inject.Inject

class GetMarketplaceListingsApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    /** One zero-based page of the browse list, newest first. */
    suspend operator fun invoke(page: Int, size: Int): MarketplaceListingPage =
        repository.getListings(page = page, size = size).convertToListingPage()
}
