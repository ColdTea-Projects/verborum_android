package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.MarketplacePageResponse
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceAccessDeniedException
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class GetMarketplaceListingsApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: MarketplaceRepository

    private lateinit var subject: GetMarketplaceListingsApiUseCase

    override fun setUp() {
        super.setUp()
        subject = GetMarketplaceListingsApiUseCase(repository)
    }

    private fun httpError(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody("application/json".toMediaType())))

    @Test
    fun `sends pairs as wire values and the other filters as given`() = runTest {
        coEvery { repository.getListings(any(), any(), any(), any(), any(), any()) } returns MarketplacePageResponse()

        subject(
            order = MarketplaceListingOrder.POPULAR,
            page = 1,
            size = 15,
            filter = MarketplaceListingFilter(
                languagePairs = listOf(MarketplaceLanguagePair("DE", "TR"), MarketplaceLanguagePair("EN", "TR")),
                tags = setOf("travel"),
                publisherName = "ann",
            ),
        )

        coVerify {
            repository.getListings(
                order = MarketplaceListingOrder.POPULAR,
                page = 1,
                size = 15,
                pairs = listOf("DE-TR", "EN-TR"),
                tags = listOf("travel"),
                publisherName = "ann",
            )
        }
    }

    @Test
    fun `a 403 right after joining is retried once`() = runTest {
        coEvery { repository.getListings(any(), any(), any(), any(), any(), any()) } throws
            httpError(403) andThen MarketplacePageResponse(hasNext = true)

        assertTrue(subject(order = MarketplaceListingOrder.NEWEST, page = 0, size = 15, filter = MarketplaceListingFilter()).hasMore)
        coVerify(exactly = 2) { repository.getListings(any(), any(), any(), any(), any(), any()) }
    }

    @Test(expected = MarketplaceAccessDeniedException::class)
    fun `a second 403 means the user is not a member`() = runTest {
        coEvery { repository.getListings(any(), any(), any(), any(), any(), any()) } throws httpError(403)

        subject(order = MarketplaceListingOrder.NEWEST, page = 0, size = 15, filter = MarketplaceListingFilter())
    }

    @Test(expected = HttpException::class)
    fun `other failures are not retried`() = runTest {
        coEvery { repository.getListings(any(), any(), any(), any(), any(), any()) } throws httpError(500)

        try {
            subject(order = MarketplaceListingOrder.NEWEST, page = 0, size = 15, filter = MarketplaceListingFilter())
        } finally {
            coVerify(exactly = 1) { repository.getListings(any(), any(), any(), any(), any(), any()) }
        }
    }
}
