package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response

class UploadServiceTest : BaseTest() {

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryApiUseCase: SaveDictionaryApiUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase

    @MockK
    private lateinit var saveWordApiUseCase: SaveWordApiUseCase

    private lateinit var uploadService: UploadService

    override fun setUp() {
        super.setUp()
        uploadService = UploadService(
            saveDictionaryApiUseCase = saveDictionaryApiUseCase,
            deleteDictionaryApiUseCase = deleteDictionaryApiUseCase,
            saveWordApiUseCase = saveWordApiUseCase,
        )
    }

    // region createDictionary

    @Test
    fun `createDictionary delegates the dictionary and returns the api response`() = runTest {
        val dictionary = testDictionary()
        val response = mockk<Response<Unit>>()
        coEvery { saveDictionaryApiUseCase.invoke(dictionary) } returns response

        val result = uploadService.createDictionary(dictionary)

        assertSame(response, result)
        coVerify(exactly = 1) { saveDictionaryApiUseCase.invoke(dictionary) }
    }

    // endregion

    // region deleteDictionary

    @Test
    fun `deleteDictionary delegates the dictionaryId and returns the api response`() = runTest {
        val response = mockk<Response<Unit>>()
        coEvery { deleteDictionaryApiUseCase.invoke("dict-1") } returns response

        val result = uploadService.deleteDictionary("dict-1")

        assertSame(response, result)
        coVerify(exactly = 1) { deleteDictionaryApiUseCase.invoke("dict-1") }
    }

    // endregion

    // region createWord

    @Test
    fun `createWord delegates the word to SaveWordApiUseCase`() = runTest {
        val word = testWord()

        uploadService.createWord(word)

        coVerify(exactly = 1) { saveWordApiUseCase.invoke(word) }
    }

    // endregion
}
