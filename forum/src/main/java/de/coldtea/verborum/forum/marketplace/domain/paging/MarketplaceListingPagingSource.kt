package de.coldtea.verborum.forum.marketplace.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import kotlinx.coroutines.CancellationException
import java.util.concurrent.ConcurrentHashMap

/**
 * Pages the marketplace browse list, keyed by the backend's zero-based page number
 * (`docs/marketplace-client-guide.md` §7.1: `nextKey = page + 1` while more pages exist).
 *
 * Append-only: every generation starts at page 0 and never prepends, which is what lets
 * [seenIds] drop a listing a previous page already delivered — the backend can repeat one when a
 * listing is published mid-scroll (§4.1), and a repeated key would crash the lazy list.
 * A refresh builds a new source, so the set never outlives its generation.
 */
class MarketplaceListingPagingSource(
    private val getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase,
    private val pageSize: Int,
) : PagingSource<Int, MarketplaceListing>() {

    private val seenIds: MutableSet<String> = ConcurrentHashMap.newKeySet()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MarketplaceListing> {
        val page = params.key ?: FIRST_PAGE
        return try {
            // Always [pageSize], never params.loadSize: page numbers only line up with a fixed size.
            val result = getMarketplaceListingsApiUseCase(page = page, size = pageSize)
            LoadResult.Page(
                data = result.listings.filter { seenIds.add(it.dictionaryId) },
                prevKey = null,
                nextKey = if (result.hasMore) page + 1 else null,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /** A refresh restarts from the newest listings rather than the scroll position. */
    override fun getRefreshKey(state: PagingState<Int, MarketplaceListing>): Int? = null

    companion object {
        const val FIRST_PAGE = 0
    }
}
