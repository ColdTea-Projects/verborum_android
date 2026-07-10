package de.coldtea.verborum.bibliotheca.word.domain.usecase.api

import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response

class DeleteWordByDictionaryIdApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordApi: WordApi

    private lateinit var useCase: DeleteWordByDictionaryIdApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteWordByDictionaryIdApiUseCase(wordApi)
    }

    // region invoke

    @Test
    fun `invoke delegates the dictionaryId to the api`() = runTest {
        coEvery { wordApi.deleteWordsByDictionaryId(any()) } returns mockk()

        useCase.invoke("dict-1")

        coVerify(exactly = 1) { wordApi.deleteWordsByDictionaryId("dict-1") }
    }

    @Test
    fun `invoke returns the api response unchanged`() = runTest {
        val response = mockk<Response<Unit>>()
        coEvery { wordApi.deleteWordsByDictionaryId("dict-1") } returns response

        val result = useCase.invoke("dict-1")

        assertSame(response, result)
    }

    // endregion
}
