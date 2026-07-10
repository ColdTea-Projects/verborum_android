package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpsertWordsUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.Response

class UploadPendingChangesUseCaseTest : BaseTest() {

    // invoke returns List<Dictionary> — stubbed per test with coEvery.
    @MockK
    private lateinit var getAllDictionariesUseCase: GetAllDictionariesUseCase

    // invoke returns List<Word> — stubbed per test with coEvery.
    @MockK
    private lateinit var getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryApiUseCase: SaveDictionaryApiUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var saveWordApiUseCase: SaveWordApiUseCase

    // invoke returns String — stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryUseCase: SaveDictionaryUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var upsertWordsUseCase: UpsertWordsUseCase

    private lateinit var useCase: UploadPendingChangesUseCase

    private val successResponse = mockk<Response<Unit>> { every { isSuccessful } returns true }
    private val failureResponse = mockk<Response<Unit>> { every { isSuccessful } returns false }

    override fun setUp() {
        super.setUp()
        useCase = UploadPendingChangesUseCase(
            getAllDictionariesUseCase = getAllDictionariesUseCase,
            getWordsByDictionaryUseCase = getWordsByDictionaryUseCase,
            saveDictionaryApiUseCase = saveDictionaryApiUseCase,
            saveWordApiUseCase = saveWordApiUseCase,
            saveDictionaryUseCase = saveDictionaryUseCase,
            upsertWordsUseCase = upsertWordsUseCase,
        )
        coEvery { getWordsByDictionaryUseCase.invoke(any()) } returns emptyList()
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "saved-id"
    }

    // region dictionaries

    @Test
    fun `invoke uploads unsynced dictionaries and marks them synced on success`() = runTest {
        val pending = testDictionary(dictionaryId = "dict-1", isSynced = false)
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(pending)
        coEvery { saveDictionaryApiUseCase.invoke(pending) } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { saveDictionaryApiUseCase.invoke(pending) }
        coVerify(exactly = 1) { saveDictionaryUseCase.invoke(pending.copy(isSynced = true)) }
    }

    @Test
    fun `invoke keeps a dictionary unsynced when the upload fails`() = runTest {
        val pending = testDictionary(dictionaryId = "dict-1", isSynced = false)
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(pending)
        coEvery { saveDictionaryApiUseCase.invoke(pending) } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke does not upload dictionaries that are already synced`() = runTest {
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "dict-1", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryApiUseCase.invoke(any()) }
    }

    // endregion

    // region words

    @Test
    fun `invoke uploads unsynced words and marks them synced on success`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        val pendingWord = testWord(wordId = "word-1", isSynced = false)
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(pendingWord)
        coEvery { saveWordApiUseCase.invoke(pendingWord) } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { saveWordApiUseCase.invoke(pendingWord) }
        coVerify(exactly = 1) { upsertWordsUseCase.invoke(listOf(pendingWord.copy(isSynced = true))) }
    }

    @Test
    fun `invoke keeps a word unsynced when the upload fails`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        val pendingWord = testWord(wordId = "word-1", isSynced = false)
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(pendingWord)
        coEvery { saveWordApiUseCase.invoke(pendingWord) } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
    }

    @Test
    fun `invoke does not upload words that are already synced`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        coEvery { getAllDictionariesUseCase.invoke() } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 0) { saveWordApiUseCase.invoke(any()) }
    }

    // endregion
}
