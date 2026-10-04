package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.data.api.MarketplaceApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedDictionaryApi
import de.coldtea.verborum.forum.marketplace.data.api.SharedWordApi
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplacePageResponse
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarketplaceRepositoryTest : BaseTest() {

    @MockK
    private lateinit var marketplaceApi: MarketplaceApi

    @MockK
    private lateinit var sharedDictionaryApi: SharedDictionaryApi

    @MockK
    private lateinit var sharedWordApi: SharedWordApi

    private lateinit var subject: MarketplaceRepository

    override fun setUp() {
        super.setUp()
        subject = MarketplaceRepository(marketplaceApi, sharedDictionaryApi, sharedWordApi)
    }

    private fun listing(dictionaryId: String) = MarketplaceListingResponse(
        dictionaryId = dictionaryId,
        publisherId = "765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6",
        publisherName = "Anna Bauer",
        name = "Polish → Ukrainian",
        fromLang = "PL",
        toLang = "UK",
    )

    @Test
    fun `getListings passes every filter through to the marketplace`() = runTest {
        coEvery { marketplaceApi.getListings(any(), any(), any(), any(), any()) } returns MarketplacePageResponse()

        subject.getListings(order = MarketplaceListingOrder.NEWEST, page = 2, size = 15, pairs = listOf("EN-TR"), tags = listOf("food"), publisherName = "ann")

        coVerify { marketplaceApi.getListings(page = 2, size = 15, pairs = listOf("EN-TR"), tags = listOf("food"), publisher = "ann") }
    }

    @Test
    fun `a loaded listing can be looked up by id afterwards`() = runTest {
        coEvery { marketplaceApi.getListings(any(), any(), any(), any(), any()) } returns
            MarketplacePageResponse(items = listOf(listing("a"), listing("b")), hasNext = false)

        subject.getListings(order = MarketplaceListingOrder.NEWEST, page = 0, size = 15, pairs = emptyList(), tags = emptyList(), publisherName = null)

        assertEquals(listing("b"), subject.getLoadedListing("b"))
        assertNull(subject.getLoadedListing("never-loaded"))
    }

    @Test
    fun `a reload replaces the remembered listing with the fresh one`() = runTest {
        val renamed = listing("a").copy(name = "Renamed")
        coEvery { marketplaceApi.getListings(any(), any(), any(), any(), any()) } returnsMany listOf(
            MarketplacePageResponse(items = listOf(listing("a"))),
            MarketplacePageResponse(items = listOf(renamed)),
        )

        repeat(2) { subject.getListings(order = MarketplaceListingOrder.NEWEST, page = 0, size = 15, pairs = emptyList(), tags = emptyList(), publisherName = null) }

        assertEquals(renamed, subject.getLoadedListing("a"))
    }

    @Test
    fun `the popular order reads the popular endpoint with the same filters`() = runTest {
        coEvery { marketplaceApi.getPopularListings(any(), any(), any(), any(), any()) } returns
            MarketplacePageResponse(items = listOf(listing("p")))

        subject.getListings(
            order = MarketplaceListingOrder.POPULAR,
            page = 0,
            size = 15,
            pairs = listOf("DE-EN"),
            tags = emptyList(),
            publisherName = null,
        )

        coVerify { marketplaceApi.getPopularListings(page = 0, size = 15, pairs = listOf("DE-EN"), tags = emptyList(), publisher = null) }
        coVerify(exactly = 0) { marketplaceApi.getListings(any(), any(), any(), any(), any()) }
        // Popular listings feed the details screen just like fresh ones.
        assertEquals(listing("p"), subject.getLoadedListing("p"))
    }
}
