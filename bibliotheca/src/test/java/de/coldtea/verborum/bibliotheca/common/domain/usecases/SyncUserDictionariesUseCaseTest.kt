package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.bibliotheca.testDictionaryResponse
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.testWordResponse
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpsertWordsUseCase
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
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

    // invoke returns List<Dictionary> — stubbed per test with coEvery.
    @MockK
    private lateinit var getAllDictionariesUseCase: GetAllDictionariesUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var deleteDictionaryUseCase: DeleteDictionaryUseCase

    // invoke returns List<Word> — stubbed per test with coEvery.
    @MockK
    private lateinit var getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var upsertWordsUseCase: UpsertWordsUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var deleteWordUseCase: DeleteWordUseCase

    private lateinit var useCase: SyncUserDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = SyncUserDictionariesUseCase(
            dictionaryApi = dictionaryApi,
            wordApi = wordApi,
            saveDictionaryUseCase = saveDictionaryUseCase,
            getAllDictionariesUseCase = getAllDictionariesUseCase,
            deleteDictionaryUseCase = deleteDictionaryUseCase,
            getWordsByDictionaryUseCase = getWordsByDictionaryUseCase,
            upsertWordsUseCase = upsertWordsUseCase,
            deleteWordUseCase = deleteWordUseCase,
        )
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "saved-id"
        coEvery { getAllDictionariesUseCase.invoke() } returns emptyList()
        coEvery { getWordsByDictionaryUseCase.invoke(any()) } returns emptyList()
    }

    // region no server information

    @Test
    fun `invoke touches nothing when the api returns null`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns null

        useCase.invoke()

        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    @Test
    fun `invoke leaves words untouched when the word api returns null`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns null

        useCase.invoke()

        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    // endregion

    // region dictionary merge

    @Test
    fun `invoke upserts every remote dictionary converted to domain model`() = runTest {
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
    fun `invoke keeps local unsynced dictionaries that the server does not know`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "local-only", isSynced = false))

        useCase.invoke()

        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke deletes local synced dictionaries that are gone remotely`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "deleted-remotely", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("deleted-remotely") }
    }

    @Test
    fun `invoke does not overwrite a dictionary with unsynced local changes`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "dict-1", isSynced = false))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns null

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    // endregion

    // region word merge

    @Test
    fun `invoke upserts the remote words converted with the response dictionaryId`() = runTest {
        val wordResponse = testWordResponse(wordId = "word-1")
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns listOf(wordResponse)
        val upserted = mutableListOf<List<Word>>()
        coEvery { upsertWordsUseCase.invoke(capture(upserted)) } returns Unit

        useCase.invoke()

        assertEquals(listOf(wordResponse.convertToWord("dict-1")), upserted.single())
    }

    @Test
    fun `invoke keeps local unsynced words that the server does not know`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns emptyList()
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "local-only", isSynced = false))

        useCase.invoke()

        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    @Test
    fun `invoke deletes local synced words that are gone remotely`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns emptyList()
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "deleted-remotely", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 1) { deleteWordUseCase.invoke("deleted-remotely") }
    }

    @Test
    fun `invoke does not overwrite words with unsynced local changes`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns
            listOf(testWordResponse(wordId = "word-1"))
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isSynced = false))

        useCase.invoke()

        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    // endregion

    // region deletion tombstones

    @Test
    fun `invoke never resurrects a tombstoned dictionary or its words`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "dict-1", isSynced = true, isDeleted = true))

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
        // While the server still lists it, the tombstone stays for the upload phase to handle.
        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
        coVerify(exactly = 0) { wordApi.getWordsByDictionary(any()) }
    }

    @Test
    fun `invoke hard-deletes a tombstoned dictionary once it is gone remotely`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()
        coEvery { getAllDictionariesUseCase.invoke() } returns
            listOf(testDictionary(dictionaryId = "dict-1", isSynced = true, isDeleted = true))

        useCase.invoke()

        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("dict-1") }
    }

    @Test
    fun `invoke never resurrects a tombstoned word`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns
            listOf(testWordResponse(wordId = "word-1"))
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isSynced = true, isDeleted = true))

        useCase.invoke()

        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
        // While the server still lists it, the tombstone stays for the upload phase to handle.
        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    @Test
    fun `invoke hard-deletes a tombstoned word once it is gone remotely`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns emptyList()
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isSynced = true, isDeleted = true))

        useCase.invoke()

        coVerify(exactly = 1) { deleteWordUseCase.invoke("word-1") }
    }

    // endregion
}
