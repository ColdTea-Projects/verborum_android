package de.coldtea.verborum.forum.marketplace.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import de.coldtea.verborum.forum.marketplace.domain.paging.MarketplaceListingPagingSource
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The only marketplace API the Forum ViewModels talk to. No local copy to observe — the
 * marketplace is online-only (guide §5.6) — so the browse list is paged straight from the API
 * and the rest are one-shot reads.
 */
class MarketplaceService @Inject constructor(
    private val getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase,
    private val getMarketplaceListingApiUseCase: GetMarketplaceListingApiUseCase,
    private val getMarketplaceWordsApiUseCase: GetMarketplaceWordsApiUseCase,
) {
    /** The browse list, newest first, loaded [LISTING_PAGE_SIZE] listings at a time. */
    fun getDictionaries(): Flow<PagingData<ForumDictionaryUi>> =
        Pager(
            config = PagingConfig(
                pageSize = LISTING_PAGE_SIZE,
                // Paging's default first load is 3 pages; page-number keys need every load equal.
                initialLoadSize = LISTING_PAGE_SIZE,
                prefetchDistance = LISTING_PAGE_SIZE / 2,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MarketplaceListingPagingSource(getMarketplaceListingsApiUseCase, LISTING_PAGE_SIZE)
            },
        ).flow.map { pagingData -> pagingData.map(MarketplaceListing::convertToUi) }

    suspend fun getDictionary(dictionaryId: String): ForumDictionaryUi? =
        getMarketplaceListingApiUseCase(dictionaryId)?.convertToUi()

    suspend fun getWords(dictionaryId: String): List<ForumWordUi> =
        getMarketplaceWordsApiUseCase(dictionaryId).map(MarketplaceWord::convertToUi)

    companion object {
        /** Deliberately below the backend's 20 default (it accepts 1–100) to keep pages light. */
        const val LISTING_PAGE_SIZE = 15
    }
}
