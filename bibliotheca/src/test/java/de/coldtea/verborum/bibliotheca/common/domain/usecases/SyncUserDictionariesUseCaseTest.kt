package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.CleanDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.testDictionaryResponse
import de.coldtea.verborum.bibliotheca.testWordResponse
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncUserDictionariesUseCaseTest : BaseTest() {

    @MockK
    private lateinit var dictionaryApi: DictionaryApi

    @MockK
    private lateinit var wordApi: WordApi

    // invoke returns String — stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryUseCase: SaveDictionaryUseCase

    @MockK
    private lateinit var saveWordUseCase: SaveWordUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var cleanDictionariesUseCase: CleanDictionariesUseCase

    private lateinit var useCase: SyncUserDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = SyncUserDictionariesUseCase(
            dictionaryApi = dictionaryApi,
            wordApi = wordApi,
            saveDictionaryUseCase = saveDictionaryUseCase,
            saveWordUseCase = saveWordUseCase,
            removeAllDictionariesUseCase = cleanDictionariesUseCase,
        )
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "saved-id"
    }

    // region invoke

    @Test
    fun `invoke fetches the guest user's dictionaries from the api`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(any()) } returns null

        useCase.invoke()

        coVerify(exactly = 1) { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) }
    }

    @Test
    fun `invoke cleans local dictionaries before saving the remote ones`() = runTest {
        // Regression guard: the cleanup parameter was previously typed GetAllDictionariesUseCase,
        // making the pre-sync removal a no-op read.
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns null

        useCase.invoke()

        coVerifyOrder {
            cleanDictionariesUseCase.invoke()
            saveDictionaryUseCase.invoke(any())
        }
    }

    @Test
    fun `invoke saves every remote dictionary converted to domain model`() = runTest {
        val responses = listOf(
            testDictionaryResponse(dictionaryId = "dict-1"),
            testDictionaryResponse(dictionaryId = "dict-2"),
        )
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns responses
        coEvery { wordApi.getWordsByDictionary(any()) } returns null
        val savedDictionaries = mutableListOf<Dictionary>()
        coEvery { saveDictionaryUseCase.invoke(capture(savedDictionaries)) } returns "saved-id"

        useCase.invoke()

        assertEquals(responses.map { it.convertToDictionary() }, savedDictionaries)
    }

    @Test
    fun `invoke saves the words of each dictionary converted with the response dictionaryId`() = runTest {
        val response = testDictionaryResponse(dictionaryId = "dict-1")
        val wordResponse = testWordResponse(wordId = "word-1")
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns listOf(response)
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns listOf(wordResponse)
        val savedWords = mutableListOf<Word>()
        coEvery { saveWordUseCase.invoke(capture(savedWords)) } returns Unit

        useCase.invoke()

        assertEquals(listOf(wordResponse.convertToWord("dict-1")), savedWords)
    }

    @Test
    fun `invoke saves nothing when the api returns no dictionaries`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns null

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveWordUseCase.invoke(any()) }
    }

    @Test
    fun `invoke still saves the dictionary when its words are null`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns null

        useCase.invoke()

        coVerify(exactly = 1) { saveDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveWordUseCase.invoke(any()) }
    }

    // endregion
}
