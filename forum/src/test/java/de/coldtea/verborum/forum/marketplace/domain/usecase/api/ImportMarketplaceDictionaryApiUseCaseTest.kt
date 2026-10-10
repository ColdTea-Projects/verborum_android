package de.coldtea.verborum.forum.marketplace.domain.usecase.api

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.data.MarketplaceRepository
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ImportMarketplaceDictionaryApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: MarketplaceRepository

    private lateinit var subject: ImportMarketplaceDictionaryApiUseCase

    override fun setUp() {
        super.setUp()
        subject = ImportMarketplaceDictionaryApiUseCase(repository)
    }

    private fun status(code: Int): Response<Unit> =
        if (code in 200..299) Response.success(code, Unit)
        else Response.error(code, "".toResponseBody("application/json".toMediaType()))

    @Test
    fun `the guide's status codes map to results`() = runTest {
        mapOf(
            201 to MarketplaceImportResult.Imported,
            403 to MarketplaceImportResult.NotMember,
            404 to MarketplaceImportResult.Unavailable,
            400 to MarketplaceImportResult.Failed,
        ).forEach { (code, expected) ->
            coEvery { repository.importDictionary("d1") } returns status(code)
            assertEquals("HTTP $code", expected, subject("d1"))
        }
    }

    @Test
    fun `a 500 from the double-import race is retried once`() = runTest {
        coEvery { repository.importDictionary("d1") } returnsMany listOf(status(500), status(201))

        assertEquals(MarketplaceImportResult.Imported, subject("d1"))
        coVerify(exactly = 2) { repository.importDictionary("d1") }
    }

    @Test
    fun `being offline is a failure`() = runTest {
        coEvery { repository.importDictionary("d1") } throws IOException("offline")

        assertEquals(MarketplaceImportResult.Failed, subject("d1"))
    }
}
