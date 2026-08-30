package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SyncDictionaryTagsUseCase
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionariesByUserUseCase
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
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val FIXED_NOW = 1_700_000_000_000L

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
    private lateinit var getDictionariesByUserUseCase: GetDictionariesByUserUseCase

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

    // invoke returns String? — stubbed in setUp to the signed-in subject.
    @MockK
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    // pull/push return values — stubbed per test; default to "no tags on the server".
    @MockK
    private lateinit var syncDictionaryTagsUseCase: SyncDictionaryTagsUseCase

    private lateinit var useCase: SyncUserDictionariesUseCase

    override fun setUp() {
        super.setUp()
        useCase = SyncUserDictionariesUseCase(
            dictionaryApi = dictionaryApi,
            wordApi = wordApi,
            saveDictionaryUseCase = saveDictionaryUseCase,
            getDictionariesByUserUseCase = getDictionariesByUserUseCase,
            deleteDictionaryUseCase = deleteDictionaryUseCase,
            getWordsByDictionaryUseCase = getWordsByDictionaryUseCase,
            upsertWordsUseCase = upsertWordsUseCase,
            deleteWordUseCase = deleteWordUseCase,
            getActiveUserUseCase = getActiveUserUseCase,
            syncDictionaryTagsUseCase = syncDictionaryTagsUseCase,
        )
        // Existing cases stub the api against GUEST_USER_ID, so pin the active user to it.
        every { getActiveUserUseCase.invoke() } returns GUEST_USER_ID
        // Default: the server has no tags, so merged dictionaries keep the empty default.
        coEvery { syncDictionaryTagsUseCase.pull(any()) } returns emptyList()
        coEvery { saveDictionaryUseCase.invoke(any()) } returns "saved-id"
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns emptyList()
        coEvery { getWordsByDictionaryUseCase.invoke(any()) } returns emptyList()

        // Newly downloaded rows stamp the current time; pin it so timestamp assertions are stable.
        mockkStatic("de.coldtea.verborum.bibliotheca.common.utils.DateTimeProviderKt")
        every { getNowInMillis() } returns FIXED_NOW
    }

    // region no server information

    @Test
    fun `invoke does not call the api when no user is signed in`() = runTest {
        every { getActiveUserUseCase.invoke() } returns null

        useCase.invoke()

        coVerify(exactly = 0) { dictionaryApi.getAllDictionariesByUser(any()) }
    }

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

        assertEquals(
            responses.map { it.convertToDictionary(fallbackCreatedAt = FIXED_NOW, fallbackUpdatedAt = FIXED_NOW) },
            savedDictionaries,
        )
    }

    @Test
    fun `invoke does not rewrite a dictionary that is already identical locally`() = runTest {
        val response = testDictionaryResponse(dictionaryId = "dict-1")
        // The exact row the merge would produce — writing it again would only churn the table.
        val identicalLocal =
            response.convertToDictionary(fallbackCreatedAt = 5_000L, fallbackUpdatedAt = 6_000L)
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns listOf(response)
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns listOf(identicalLocal)
        coEvery { wordApi.getWordsByDictionary(any()) } returns null

        useCase.invoke()

        coVerify(exactly = 0) { saveDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke still writes a dictionary whose remote content differs`() = runTest {
        val response = testDictionaryResponse(dictionaryId = "dict-1", name = "Renamed remotely")
        val staleLocal = response
            .convertToDictionary(fallbackCreatedAt = 5_000L, fallbackUpdatedAt = 6_000L)
            .copy(name = "Old name")
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns listOf(response)
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns listOf(staleLocal)
        coEvery { wordApi.getWordsByDictionary(any()) } returns null

        useCase.invoke()

        coVerify(exactly = 1) {
            saveDictionaryUseCase.invoke(
                response.convertToDictionary(fallbackCreatedAt = 5_000L, fallbackUpdatedAt = 6_000L)
            )
        }
    }

    @Test
    fun `invoke keeps local unsynced dictionaries that the server does not know`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns
            listOf(testDictionary(dictionaryId = "local-only", isSynced = false))

        useCase.invoke()

        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke deletes local synced dictionaries that are gone remotely`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns
            listOf(testDictionary(dictionaryId = "deleted-remotely", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("deleted-remotely") }
    }

    @Test
    fun `invoke only reconciles the signed-in user's own dictionaries`() = runTest {
        // The remote list covers one owner, so the local side must be read for that owner too —
        // reading every row would see another account's dictionaries as "deleted on the server".
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns emptyList()

        useCase.invoke()

        coVerify(exactly = 1) { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) }
        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke does not overwrite a dictionary with unsynced local changes`() = runTest {
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns
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

        assertEquals(
            listOf(wordResponse.convertToWord("dict-1", fallbackCreatedAt = FIXED_NOW, fallbackUpdatedAt = FIXED_NOW)),
            upserted.single(),
        )
    }

    @Test
    fun `invoke does not upsert words that are already identical locally`() = runTest {
        val wordResponse = testWordResponse(wordId = "word-1")
        val identicalLocal = wordResponse.convertToWord(
            dictionaryId = "dict-1",
            fallbackCreatedAt = 5_000L,
            fallbackUpdatedAt = 6_000L,
        )
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns listOf(wordResponse)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(identicalLocal)

        useCase.invoke()

        coVerify(exactly = 0) { upsertWordsUseCase.invoke(any()) }
    }

    @Test
    fun `invoke takes the server practice level when merging a remote word`() = runTest {
        // The server now owns the level; a synced word adopts whatever valid value it sends.
        val wordResponse = testWordResponse(wordId = "word-1", level = JsonPrimitive(5))
        val localStale = wordResponse
            .convertToWord(dictionaryId = "dict-1", fallbackCreatedAt = 5_000L, fallbackUpdatedAt = 6_000L)
            .copy(level = 2, word = "stale so the row is rewritten")
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns listOf(wordResponse)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(localStale)
        val upserted = mutableListOf<List<Word>>()
        coEvery { upsertWordsUseCase.invoke(capture(upserted)) } returns Unit

        useCase.invoke()

        val merged = upserted.single().single()
        assertEquals(5, merged.level)
        assertTrue(merged.isSynced)
    }

    @Test
    fun `invoke resets an invalid server level to zero and marks the word for re-upload`() = runTest {
        // 9 is out of the 0..7 range — reset to 0 and flag unsynced so the fix is pushed back.
        val wordResponse = testWordResponse(wordId = "word-1", level = JsonPrimitive(9))
        coEvery { dictionaryApi.getAllDictionariesByUser(GUEST_USER_ID) } returns
            listOf(testDictionaryResponse(dictionaryId = "dict-1"))
        coEvery { wordApi.getWordsByDictionary("dict-1") } returns listOf(wordResponse)
        val upserted = mutableListOf<List<Word>>()
        coEvery { upsertWordsUseCase.invoke(capture(upserted)) } returns Unit

        useCase.invoke()

        val merged = upserted.single().single()
        assertEquals(0, merged.level)
        assertFalse(merged.isSynced)
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
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns
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
        coEvery { getDictionariesByUserUseCase.invoke(GUEST_USER_ID) } returns
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
