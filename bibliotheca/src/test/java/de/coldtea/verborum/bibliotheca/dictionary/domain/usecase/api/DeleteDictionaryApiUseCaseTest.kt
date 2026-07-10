package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response

class DeleteDictionaryApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryApi: DictionaryApi

    private lateinit var useCase: DeleteDictionaryApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteDictionaryApiUseCase(dictionaryApi)
    }

    // region invoke

    @Test
    fun `invoke delegates the dictionaryId to the api`() = runTest {
        coEvery { dictionaryApi.deleteDictionary(any()) } returns mockk()

        useCase.invoke("dict-1")

        coVerify(exactly = 1) { dictionaryApi.deleteDictionary("dict-1") }
    }

    @Test
    fun `invoke returns the api response unchanged`() = runTest {
        val response = mockk<Response<Unit>>()
        coEvery { dictionaryApi.deleteDictionary("dict-1") } returns response

        val result = useCase.invoke("dict-1")

        assertSame(response, result)
    }

    // endregion
}
