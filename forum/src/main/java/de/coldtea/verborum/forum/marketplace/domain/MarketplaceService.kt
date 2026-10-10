package de.coldtea.verborum.forum.marketplace.domain

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import de.coldtea.verborum.forum.marketplace.domain.paging.MarketplaceListingPagingSource
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceWordsApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.ImportMarketplaceDictionaryApiUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.GetOwnedDictionaryLanguagesUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.ObserveDownloadedDictionaryIdsUseCase
import de.coldtea.verborum.forum.marketplace.domain.usecase.local.SaveDownloadedDictionaryUseCase
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumWordUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryUi
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import de.coldtea.verborum.forum.vault.domain.usecase.api.GetVaultDictionaryLanguagesApiUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The only marketplace API the Forum ViewModels talk to. No local copy to observe — the
 * marketplace is online-only (guide §6.9) — so the browse lists are paged straight from the API
 * and the rest are one-shot reads.
 */
class MarketplaceService @Inject constructor(
    private val getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase,
    private val getMarketplaceListingApiUseCase: GetMarketplaceListingApiUseCase,
    private val getMarketplaceWordsApiUseCase: GetMarketplaceWordsApiUseCase,
    private val getOwnedDictionaryLanguagesUseCase: GetOwnedDictionaryLanguagesUseCase,
    private val getVaultDictionaryLanguagesApiUseCase: GetVaultDictionaryLanguagesApiUseCase,
    private val profileService: ProfileService,
    private val importMarketplaceDictionaryApiUseCase: ImportMarketplaceDictionaryApiUseCase,
    private val observeDownloadedDictionaryIdsUseCase: ObserveDownloadedDictionaryIdsUseCase,
    private val saveDownloadedDictionaryUseCase: SaveDownloadedDictionaryUseCase,
    private val getActiveUserUseCase: GetActiveUserUseCase,
) {
    /** The browse list narrowed by [filter], sorted by [order], loaded [LISTING_PAGE_SIZE] at a time. */
    fun getDictionaries(
        filter: ForumDictionaryFilter,
        order: MarketplaceListingOrder,
    ): Flow<PagingData<ForumDictionaryUi>> =
        Pager(
            config = PagingConfig(
                pageSize = LISTING_PAGE_SIZE,
                // Paging's default first load is 3 pages; page-number keys need every load equal.
                initialLoadSize = LISTING_PAGE_SIZE,
                prefetchDistance = LISTING_PAGE_SIZE / 2,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MarketplaceListingPagingSource(
                    getMarketplaceListingsApiUseCase = getMarketplaceListingsApiUseCase,
                    order = order,
                    pageSize = LISTING_PAGE_SIZE,
                    filter = filter.convertToListingFilter(),
                )
            },
        ).flow.map { pagingData -> pagingData.map(MarketplaceListing::convertToUi) }

    suspend fun getDictionary(dictionaryId: String): ForumDictionaryUi? =
        getMarketplaceListingApiUseCase(dictionaryId)?.convertToUi()

    suspend fun getWords(dictionaryId: String): List<ForumWordUi> =
        getMarketplaceWordsApiUseCase(dictionaryId).map(MarketplaceWord::convertToUi)

    /** The signed-in user's `sub` — a listing with this publisherId is their own. */
    fun currentUserId(): String? = getActiveUserUseCase.invoke()

    /** The dictionaries already downloaded to this device. */
    fun observeDownloadedDictionaryIds(): Flow<Set<String>> = observeDownloadedDictionaryIdsUseCase()

    /**
     * Downloads [dictionary] (guide §6.6): the import is recorded by the marketplace, then the
     * dictionary is copied into the user's Bibliotheca straight away. That copy is best-effort —
     * the next vault refresh repairs a missed one — so only the import decides the result.
     */
    suspend fun downloadDictionary(dictionary: ForumDictionaryUi): MarketplaceImportResult {
        val result = importMarketplaceDictionaryApiUseCase(dictionary.dictionaryId)
        if (result == MarketplaceImportResult.Imported) {
            try {
                saveDownloadedDictionaryUseCase(dictionary.dictionaryId, dictionary.publisherName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Left to the vault refresh.
            }
        }
        return result
    }

    /**
     * The filter the Forum opens with: the user's [DEFAULT_PAIR_COUNT] most used language pairs
     * across their own dictionaries and the ones they imported (guide §5.1 recommends the user's
     * own pairs). Each source is best-effort — an unreachable vault still leaves the own pairs —
     * and with no dictionaries at all the filter is empty.
     */
    suspend fun getDefaultFilter(): ForumDictionaryFilter {
        val own = bestEffort { getOwnedDictionaryLanguagesUseCase() }
        val imported = bestEffort { getVaultDictionaryLanguagesApiUseCase(profileService.getUserId()) }
        val pairs = LanguagePairRanking.topPairs(own + imported, DEFAULT_PAIR_COUNT)
            .mapNotNull(ForumLanguagePair::fromListingPair)
        return ForumDictionaryFilter(languagePairs = pairs)
    }

    private suspend fun bestEffort(load: suspend () -> List<DictionaryLanguages>): List<DictionaryLanguages> =
        try {
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyList()
        }

    companion object {
        /** Deliberately below the backend's 20 default (it accepts 1–100) to keep pages light. */
        const val LISTING_PAGE_SIZE = 15

        /** How many of the user's language pairs pre-fill the filter. */
        const val DEFAULT_PAIR_COUNT = 3
    }
}
