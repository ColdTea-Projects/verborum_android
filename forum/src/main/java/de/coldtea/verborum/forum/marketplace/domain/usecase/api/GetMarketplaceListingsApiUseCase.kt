package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceAccessDeniedException
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject

class GetMarketplaceListingsApiUseCase @Inject constructor(
    private val repository: MarketplaceRepository,
) {
    /**
     * One zero-based page of the browse list narrowed by [filter], sorted by [order].
     *
     * A 403 is retried once after [MEMBERSHIP_PROPAGATION_MILLIS]: membership reaches the
     * marketplace by event, so a call right after joining can still be refused (guide §4.6). A
     * second 403 becomes [MarketplaceAccessDeniedException].
     */
    suspend operator fun invoke(
        order: MarketplaceListingOrder,
        page: Int,
        size: Int,
        filter: MarketplaceListingFilter,
    ): MarketplaceListingPage {
        val result = try {
            load(order, page, size, filter)
        } catch (e: HttpException) {
            if (e.code() != HTTP_FORBIDDEN) throw e
            delay(MEMBERSHIP_PROPAGATION_MILLIS)
            try {
                load(order, page, size, filter)
            } catch (retryFailure: HttpException) {
                if (retryFailure.code() == HTTP_FORBIDDEN) throw MarketplaceAccessDeniedException()
                throw retryFailure
            }
        }
        return result.convertToListingPage()
    }

    private suspend fun load(order: MarketplaceListingOrder, page: Int, size: Int, filter: MarketplaceListingFilter) =
        repository.getListings(
            order = order,
            page = page,
            size = size,
            pairs = filter.languagePairs.map(MarketplaceLanguagePair::wireValue),
            tags = filter.tags.toList(),
            publisherName = filter.publisherName,
        )

    private companion object {
        const val HTTP_FORBIDDEN = 403
        const val MEMBERSHIP_PROPAGATION_MILLIS = 1_000L
    }
}
