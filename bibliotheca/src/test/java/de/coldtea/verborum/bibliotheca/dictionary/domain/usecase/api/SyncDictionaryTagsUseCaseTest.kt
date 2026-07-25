package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryTagApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagRequest
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryTagResponse
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SyncDictionaryTagsUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryTagApi: DictionaryTagApi

    private lateinit var useCase: SyncDictionaryTagsUseCase

    private val ok = mockk<Response<Unit>> { every { isSuccessful } returns true }

    override fun setUp() {
        super.setUp()
        useCase = SyncDictionaryTagsUseCase(dictionaryTagApi)
    }

    private fun tag(value: String) = DictionaryTagResponse(tag = value)

    // region pull

    @Test
    fun `pull maps the response to tag codes`() = runTest {
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } returns listOf(tag("a1"), tag("food_drink"))

        assertEquals(listOf("a1", "food_drink"), useCase.pull("d1"))
    }

    @Test
    fun `pull returns null when the fetch throws`() = runTest {
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } throws RuntimeException("offline")

        assertEquals(null, useCase.pull("d1"))
    }

    // endregion

    // region push

    @Test
    fun `push adds only missing tags and deletes only extra ones`() = runTest {
        // Server has {a1, food_drink}; local wants {a1, sports} → add sports, delete food_drink.
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } returns listOf(tag("a1"), tag("food_drink"))
        coEvery { dictionaryTagApi.addDictionaryTag(any(), any()) } returns ok
        coEvery { dictionaryTagApi.deleteDictionaryTag(any(), any()) } returns ok

        val result = useCase.push("d1", listOf("a1", "sports"))

        assertTrue(result)
        coVerify(exactly = 1) { dictionaryTagApi.addDictionaryTag("d1", DictionaryTagRequest("sports")) }
        coVerify(exactly = 1) { dictionaryTagApi.deleteDictionaryTag("d1", "food_drink") }
        coVerify(exactly = 0) { dictionaryTagApi.addDictionaryTag("d1", DictionaryTagRequest("a1")) }
    }

    @Test
    fun `push returns false when the tag list cannot be fetched`() = runTest {
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } throws RuntimeException("offline")

        assertFalse(useCase.push("d1", listOf("a1")))
        coVerify(exactly = 0) { dictionaryTagApi.addDictionaryTag(any(), any()) }
    }

    @Test
    fun `push returns false when an add fails`() = runTest {
        val failed = mockk<Response<Unit>> { every { isSuccessful } returns false }
        coEvery { dictionaryTagApi.getDictionaryTags("d1") } returns emptyList()
        coEvery { dictionaryTagApi.addDictionaryTag(any(), any()) } returns failed

        assertFalse(useCase.push("d1", listOf("sports")))
    }

    // endregion
}
