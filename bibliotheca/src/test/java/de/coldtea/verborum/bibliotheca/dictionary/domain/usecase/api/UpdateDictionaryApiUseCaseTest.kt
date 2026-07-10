package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryRequest
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response

class UpdateDictionaryApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryApi: DictionaryApi

    private lateinit var useCase: UpdateDictionaryApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = UpdateDictionaryApiUseCase(dictionaryApi)
    }

    // region invoke

    @Test
    fun `invoke converts the dictionary to a request and sends it via the update endpoint`() = runTest {
        val dictionary = testDictionary(
            dictionaryId = "dict-42",
            userId = "user-7",
            name = "Spanish Food",
            isPublic = true,
            fromLang = "es",
            toLang = "en",
        )
        val requestSlot = slot<DictionaryRequest>()
        coEvery { dictionaryApi.updateDictionary(capture(requestSlot)) } returns mockk()

        useCase.invoke(dictionary)

        with(requestSlot.captured) {
            assertEquals("dict-42", dictionaryId)
            assertEquals("user-7", userId)
            assertEquals("Spanish Food", name)
            assertEquals(true, isPublic)
            assertEquals("es", fromLang)
            assertEquals("en", toLang)
        }
    }

    @Test
    fun `invoke returns the api response unchanged`() = runTest {
        val response = mockk<Response<Unit>>()
        coEvery { dictionaryApi.updateDictionary(any()) } returns response

        val result = useCase.invoke(testDictionary())

        assertSame(response, result)
    }

    @Test
    fun `invoke never calls the create endpoint`() = runTest {
        // Regression guard: this use case previously POSTed to createDictionary by mistake.
        coEvery { dictionaryApi.updateDictionary(any()) } returns mockk()

        useCase.invoke(testDictionary())

        coVerify(exactly = 0) { dictionaryApi.createDictionary(any()) }
    }

    // endregion
}
