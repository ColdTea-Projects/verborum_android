package de.coldtea.verborum.forum.marketplace.domain.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import de.coldtea.verborum.forum.marketplace.domain.usecase.api.GetMarketplaceListingsApiUseCase
import de.coldtea.verborum.forum.testMarketplaceListing
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceListingPagingSourceTest : BaseTest() {

    @MockK
    private lateinit var getMarketplaceListingsApiUseCase: GetMarketplaceListingsApiUseCase

    private val config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE)

    private fun pager() = TestPager(
        config,
        MarketplaceListingPagingSource(getMarketplaceListingsApiUseCase, PAGE_SIZE, FILTER),
    )

    private fun page(page: Int, vararg ids: String, hasMore: Boolean = true) = MarketplaceListingPage(
        listings = ids.map { testMarketplaceListing(dictionaryId = it) },
        page = page,
        hasMore = hasMore,
    )

    @Test
    fun `refresh loads page zero with the configured size`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) } returns page(0, "a", "b")

        val result = pager().refresh() as LoadResult.Page

        assertEquals(listOf("a", "b"), result.data.map { it.dictionaryId })
        assertNull(result.prevKey)
        assertEquals(1, result.nextKey)
    }

    @Test
    fun `every page is requested with the source's filter`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, any()) } returns page(0, "a")
        coEvery { getMarketplaceListingsApiUseCase(1, PAGE_SIZE, any()) } returns page(1, "b")
        val pager = pager()

        pager.refresh()
        pager.append()

        coVerify(exactly = 1) { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) }
        coVerify(exactly = 1) { getMarketplaceListingsApiUseCase(1, PAGE_SIZE, FILTER) }
    }

    @Test
    fun `append requests the next page number, not a load-size offset`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) } returns page(0, "a")
        coEvery { getMarketplaceListingsApiUseCase(1, PAGE_SIZE, FILTER) } returns page(1, "b")
        val pager = pager()

        pager.refresh()
        val result = pager.append() as LoadResult.Page

        assertEquals(listOf("b"), result.data.map { it.dictionaryId })
        assertEquals(2, result.nextKey)
        coVerify(exactly = 1) { getMarketplaceListingsApiUseCase(1, PAGE_SIZE, FILTER) }
    }

    @Test
    fun `the last page ends pagination`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) } returns page(0, "a", hasMore = false)

        val result = pager().refresh() as LoadResult.Page

        assertNull(result.nextKey)
    }

    @Test
    fun `a listing repeated by a later page is dropped`() = runTest {
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) } returns page(0, "a", "b")
        coEvery { getMarketplaceListingsApiUseCase(1, PAGE_SIZE, FILTER) } returns page(1, "b", "c")
        val pager = pager()

        pager.refresh()
        pager.append()

        assertEquals(listOf("a", "b", "c"), pager.getPages().flatMap { it.data }.map { it.dictionaryId })
    }

    @Test
    fun `a failing load becomes an error result`() = runTest {
        val failure = RuntimeException("offline")
        coEvery { getMarketplaceListingsApiUseCase(0, PAGE_SIZE, FILTER) } throws failure

        val result = pager().refresh()

        assertTrue(result is LoadResult.Error)
        assertEquals(failure, (result as LoadResult.Error).throwable)
    }

    private companion object {
        const val PAGE_SIZE = 15
        val FILTER = MarketplaceListingFilter(fromLang = "EN", tags = setOf("a1"))
    }
}
