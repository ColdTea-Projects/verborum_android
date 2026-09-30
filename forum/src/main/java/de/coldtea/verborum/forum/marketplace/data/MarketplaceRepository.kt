package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplacePageResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceWordResponse
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * The Forum tab's data source. **Serves [MarketplaceDummyData] for now** — not wired to the backend.
 * Each function already has the shape of the call that will replace it
 * (`docs/marketplace-client-guide.md`), so only the bodies change when the Retrofit APIs arrive:
 *
 * - [getListings] → ms_marketplace `GET /marketplace/dictionaries?page=&size=` (§4.1)
 * - [getListing] → no single-listing endpoint exists; resolve from the loaded page, with
 *   ms_dictionary `GET /dictionaries/dictionary/{id}` for the dictionary itself (§4.5)
 * - [getWords] → ms_dictionary `GET /words/dictionary/{id}` (§4.5)
 */
class MarketplaceRepository @Inject constructor() {

    /** Rejects what the backend answers with a 400: `page < 0` or `size` outside 1–100 (§4.1). */
    suspend fun getListings(page: Int = 0, size: Int = DEFAULT_PAGE_SIZE): MarketplacePageResponse {
        require(page >= 0) { "page must be >= 0, was $page" }
        require(size in 1..MAX_PAGE_SIZE) { "size must be in 1..$MAX_PAGE_SIZE, was $size" }
        // A network round trip's worth of latency, so the list's loading footer is visible.
        delay(SIMULATED_LATENCY_MILLIS)

        val all = MarketplaceDummyData.listings
        return MarketplacePageResponse(
            items = all.drop(page * size).take(size),
            page = page,
            size = size,
            totalElements = all.size.toLong(),
            totalPages = (all.size + size - 1) / size,
        )
    }

    /** Null when unknown — or, once real, made private/deleted since the list loaded (404). */
    suspend fun getListing(dictionaryId: String): MarketplaceListingResponse? =
        MarketplaceDummyData.listings.firstOrNull { it.dictionaryId == dictionaryId }

    suspend fun getWords(dictionaryId: String): List<MarketplaceWordResponse> =
        MarketplaceDummyData.wordsFor(dictionaryId)

    companion object {
        /** The backend's default page size (guide §4.1). */
        const val DEFAULT_PAGE_SIZE = 20

        /** The largest page the backend serves (guide §4.1). */
        const val MAX_PAGE_SIZE = 100

        private const val SIMULATED_LATENCY_MILLIS = 600L
    }
}
