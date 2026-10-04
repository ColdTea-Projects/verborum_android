package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplaceListingResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryResponse
import de.coldtea.verborum.forum.marketplace.data.api.model.SharedDictionaryTagResponse
import de.coldtea.verborum.forum.testMarketplaceListing
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GetMarketplaceListingApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: MarketplaceRepository

    private lateinit var subject: GetMarketplaceListingApiUseCase

    private val dictionary = SharedDictionaryResponse(
        dictionaryId = "d1",
        userId = "publisherId",
        name = "Renamed by the owner",
        fromLang = "EN",
        toLang = "DE",
        createdAt = "1970-01-01T00:00:02Z",
    )

    private val loaded = MarketplaceListingResponse(
        dictionaryId = "d1",
        publisherId = "publisherId",
        publisherName = "Anna Schmidt",
        name = "Old name",
        fromLang = "EN",
        toLang = "DE",
        tags = listOf("a1", "food_drink"),
        importCount = 12,
        publishedAt = "1970-01-01T00:00:01Z",
    )

    override fun setUp() {
        super.setUp()
        coEvery { repository.getDictionary("d1") } returns dictionary
        subject = GetMarketplaceListingApiUseCase(repository)
    }

    @Test
    fun `reads the dictionary fresh and the publisher details from the loaded listing`() = runTest {
        every { repository.getLoadedListing("d1") } returns loaded

        assertEquals(
            testMarketplaceListing(dictionaryId = "d1", name = "Renamed by the owner"),
            subject("d1"),
        )
        coVerify(exactly = 0) { repository.getTags(any()) }
    }

    @Test
    fun `without a loaded listing the tags are read fresh and the publisher is unknown`() = runTest {
        every { repository.getLoadedListing("d1") } returns null
        coEvery { repository.getTags("d1") } returns listOf(SharedDictionaryTagResponse("B1"), SharedDictionaryTagResponse(null))

        assertEquals(
            testMarketplaceListing(
                dictionaryId = "d1",
                name = "Renamed by the owner",
                publisherName = null,
                importCount = 0,
                publishedAt = 2_000L,
                tags = listOf("b1"),
            ),
            subject("d1"),
        )
    }

    @Test
    fun `a dictionary made private or deleted is null`() = runTest {
        coEvery { repository.getDictionary("d1") } throws
            HttpException(Response.error<Any>(404, "".toResponseBody("application/json".toMediaType())))

        assertNull(subject("d1"))
    }
}
