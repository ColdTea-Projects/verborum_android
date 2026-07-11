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

class DeleteWordApiUseCaseTest : BaseTest() {

    @MockK
    private lateinit var wordApi: WordApi

    private lateinit var useCase: DeleteWordApiUseCase

    override fun setUp() {
        super.setUp()
        useCase = DeleteWordApiUseCase(wordApi)
    }

    @Test
    fun `invoke delegates the wordId and returns the api response`() = runTest {
        val response = mockk<Response<Unit>>()
        coEvery { wordApi.deleteWord("word-1") } returns response

        val result = useCase.invoke("word-1")

        assertSame(response, result)
        coVerify(exactly = 1) { wordApi.deleteWord("word-1") }
    }
}
