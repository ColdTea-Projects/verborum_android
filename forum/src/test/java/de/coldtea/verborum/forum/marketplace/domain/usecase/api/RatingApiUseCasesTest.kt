package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.library.domain.model.RatingOutcome
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.data.api.model.RatingResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RatingApiUseCasesTest : BaseTest() {

    @MockK
    private lateinit var repository: MarketplaceRepository

    private fun <T> error(code: Int): Response<T> =
        Response.error(code, "".toResponseBody("application/json".toMediaType()))

    // region get
    @Test
    fun `the user's stars are read from a 200`() = runTest {
        coEvery { repository.getRating("d1") } returns Response.success(RatingResponse(dictionaryId = "d1", stars = 4))

        assertEquals(4, GetMyRatingApiUseCase(repository)("d1"))
    }

    @Test
    fun `a 404 means not rated yet`() = runTest {
        coEvery { repository.getRating("d1") } returns error(404)

        assertNull(GetMyRatingApiUseCase(repository)("d1"))
    }

    @Test(expected = HttpException::class)
    fun `any other failure is not read as not rated`() = runTest {
        coEvery { repository.getRating("d1") } returns error(500)

        GetMyRatingApiUseCase(repository)("d1")
    }
    // endregion

    // region set
    @Test
    fun `the guide's status codes map to outcomes`() = runTest {
        val useCase = SetRatingApiUseCase(repository)
        mapOf(
            201 to RatingOutcome.DONE,
            404 to RatingOutcome.UNAVAILABLE,
            403 to RatingOutcome.NOT_ALLOWED,
            400 to RatingOutcome.NOT_ALLOWED,
            500 to RatingOutcome.FAILED,
        ).forEach { (code, expected) ->
            coEvery { repository.setRating("d1", 4) } returns
                (if (code in 200..299) Response.success(code, Unit) else error(code))
            assertEquals("HTTP $code", expected, useCase("d1", 4))
        }
    }

    @Test
    fun `offline is a failure, not an exception`() = runTest {
        coEvery { repository.setRating("d1", 4) } throws IOException("offline")

        assertEquals(RatingOutcome.FAILED, SetRatingApiUseCase(repository)("d1", 4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `stars outside 1 to 5 are never sent`() = runTest {
        try {
            SetRatingApiUseCase(repository)("d1", 6)
        } finally {
            coVerify(exactly = 0) { repository.setRating(any(), any()) }
        }
    }
    // endregion

    // region remove
    @Test
    fun `removing a rating is done on 200 and fails offline quietly`() = runTest {
        val useCase = RemoveRatingApiUseCase(repository)
        coEvery { repository.removeRating("d1") } returns Response.success(Unit)
        assertEquals(RatingOutcome.DONE, useCase("d1"))

        coEvery { repository.removeRating("d1") } throws IOException("offline")
        assertEquals(RatingOutcome.FAILED, useCase("d1"))
    }
    // endregion
}
