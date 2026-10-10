package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.forum.marketplace.data.api.MarketplaceApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedDictionaryApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedWordApi
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplacePageResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceWordResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryTagResponse
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import retrofit2.Response
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Forum's data source — always the live backend, nothing persisted (`docs/marketplace-client-
 * guide.md`): ms_marketplace for the browse list, ms_dictionary for a shared dictionary's
 * details and words.
 *
 * The listings of loaded pages are remembered in memory for the session, because the
 * marketplace has no single-listing endpoint: the details screen takes the publisher's name and
 * the import count from there. Everything else on that screen is read fresh.
 */
@Singleton
class MarketplaceRepository @Inject constructor(
    private val marketplaceApi: MarketplaceApi,
    private val sharedDictionaryApi: SharedDictionaryApi,
    private val sharedWordApi: SharedWordApi,
) {
    private val loadedListings = ConcurrentHashMap<String, MarketplaceListingResponse>()

    suspend fun getListings(
        order: MarketplaceListingOrder,
        page: Int,
        size: Int,
        pairs: List<String>,
        tags: List<String>,
        publisherName: String?,
    ): MarketplacePageResponse =
        when (order) {
            MarketplaceListingOrder.NEWEST ->
                marketplaceApi.getListings(page = page, size = size, pairs = pairs, tags = tags, publisher = publisherName)
            MarketplaceListingOrder.POPULAR ->
                marketplaceApi.getPopularListings(page = page, size = size, pairs = pairs, tags = tags, publisher = publisherName)
        }.also { response -> response.items.forEach { loadedListings[it.dictionaryId] = it } }

    suspend fun importDictionary(dictionaryId: String): Response<Unit> =
        marketplaceApi.importDictionary(dictionaryId)

    /** The listing as the browse list last delivered it; null if it was never loaded this session. */
    fun getLoadedListing(dictionaryId: String): MarketplaceListingResponse? = loadedListings[dictionaryId]

    suspend fun getDictionary(dictionaryId: String): SharedDictionaryResponse =
        sharedDictionaryApi.getDictionary(dictionaryId)

    suspend fun getTags(dictionaryId: String): List<SharedDictionaryTagResponse> =
        sharedDictionaryApi.getTags(dictionaryId)

    suspend fun getWords(dictionaryId: String): List<MarketplaceWordResponse> =
        sharedWordApi.getWords(dictionaryId)
}
