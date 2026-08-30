package de.coldtea.verborum.bibliotheca.common.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SyncDictionaryTagsUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionariesByUserUseCase
import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionarySyncedUseCase
import de.coldtea.verborum.bibliotheca.testDictionary
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.MarkWordSyncedUseCase
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
    private lateinit var getDictionariesByUserUseCase: GetDictionariesByUserUseCase

    @MockK
    private lateinit var getActiveUserUseCase: GetActiveUserUseCase

    // invoke returns List<Word> — stubbed per test with coEvery.
    @MockK
    private lateinit var getWordsByDictionaryUseCase: GetWordsByDictionaryUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var saveDictionaryApiUseCase: SaveDictionaryApiUseCase

    // push returns Boolean — defaults to a successful tag reconcile in setUp.
    @MockK
    private lateinit var syncDictionaryTagsUseCase: SyncDictionaryTagsUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var saveWordApiUseCase: SaveWordApiUseCase

    // invoke returns String — stubbed per test with coEvery.
    @MockK
    private lateinit var markDictionarySyncedUseCase: MarkDictionarySyncedUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var markWordSyncedUseCase: MarkWordSyncedUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteWordApiUseCase: DeleteWordApiUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase

    // invoke returns Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var deleteDictionaryUseCase: DeleteDictionaryUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordUseCase: DeleteWordUseCase

    private lateinit var useCase: UploadPendingChangesUseCase

    private val successResponse = mockk<Response<Unit>> { every { isSuccessful } returns true }
    private val failureResponse = mockk<Response<Unit>> { every { isSuccessful } returns false }

    override fun setUp() {
        super.setUp()
        useCase = UploadPendingChangesUseCase(
            getActiveUserUseCase = getActiveUserUseCase,
            getDictionariesByUserUseCase = getDictionariesByUserUseCase,
            getWordsByDictionaryUseCase = getWordsByDictionaryUseCase,
            saveDictionaryApiUseCase = saveDictionaryApiUseCase,
            syncDictionaryTagsUseCase = syncDictionaryTagsUseCase,
            saveWordApiUseCase = saveWordApiUseCase,
            markDictionarySyncedUseCase = markDictionarySyncedUseCase,
            markWordSyncedUseCase = markWordSyncedUseCase,
            deleteDictionaryApiUseCase = deleteDictionaryApiUseCase,
            deleteWordApiUseCase = deleteWordApiUseCase,
            deleteWordByDictionaryIdApiUseCase = deleteWordByDictionaryIdApiUseCase,
            deleteDictionaryUseCase = deleteDictionaryUseCase,
            deleteWordUseCase = deleteWordUseCase,
        )
        coEvery { getWordsByDictionaryUseCase.invoke(any()) } returns emptyList()
        // Tag reconcile succeeds by default so dictionaries reach the synced state as before.
        coEvery { syncDictionaryTagsUseCase.push(any(), any()) } returns true
        // Signed in by default; the signed-out path is covered in its own region.
        every { getActiveUserUseCase.invoke() } returns SUBJECT
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns emptyList()
    }

    // region signed-out

    @Test
    fun `invoke uploads nothing while signed out`() = runTest {
        // Guest rows carry GUEST_USER_ID, which must never reach the server — they go up only
        // after login re-owns them under the real subject.
        every { getActiveUserUseCase.invoke() } returns null

        useCase.invoke()

        coVerify(exactly = 0) { getDictionariesByUserUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveDictionaryApiUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveWordApiUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteDictionaryApiUseCase.invoke(any()) }
        coVerify(exactly = 0) { deleteWordApiUseCase.invoke(any()) }
    }

    @Test
    fun `invoke only uploads the signed-in user's own dictionaries`() = runTest {
        useCase.invoke()

        coVerify(exactly = 1) { getDictionariesByUserUseCase.invoke(SUBJECT) }
    }

    // endregion

    // region dictionaries

    @Test
    fun `invoke uploads unsynced dictionaries and marks them synced on success`() = runTest {
        val pending = testDictionary(dictionaryId = "dict-1", isSynced = false)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(pending)
        coEvery { saveDictionaryApiUseCase.invoke(pending) } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { saveDictionaryApiUseCase.invoke(pending) }
        coVerify(exactly = 1) {
            markDictionarySyncedUseCase.invoke(
                dictionaryId = pending.dictionaryId,
                updatedAt = pending.updatedAt,
            )
        }
    }

    @Test
    fun `invoke keeps a dictionary unsynced when the upload fails`() = runTest {
        val pending = testDictionary(dictionaryId = "dict-1", isSynced = false)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(pending)
        coEvery { saveDictionaryApiUseCase.invoke(pending) } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { markDictionarySyncedUseCase.invoke(any(), any()) }
    }

    @Test
    fun `invoke does not upload dictionaries that are already synced`() = runTest {
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns
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
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(pendingWord)
        coEvery { saveWordApiUseCase.invoke(pendingWord) } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { saveWordApiUseCase.invoke(pendingWord) }
        coVerify(exactly = 1) {
            markWordSyncedUseCase.invoke(
                wordId = pendingWord.wordId,
                updatedAt = pendingWord.updatedAt,
            )
        }
    }

    @Test
    fun `invoke keeps a word unsynced when the upload fails`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        val pendingWord = testWord(wordId = "word-1", isSynced = false)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns listOf(pendingWord)
        coEvery { saveWordApiUseCase.invoke(pendingWord) } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { markWordSyncedUseCase.invoke(any(), any()) }
    }

    @Test
    fun `invoke does not upload words that are already synced`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isSynced = true))

        useCase.invoke()

        coVerify(exactly = 0) { saveWordApiUseCase.invoke(any()) }
    }

    // endregion

    // region pending deletions

    @Test
    fun `invoke deletes a tombstoned dictionary remotely then hard-deletes it locally`() = runTest {
        val tombstoned = testDictionary(dictionaryId = "dict-1", isDeleted = true)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(tombstoned)
        coEvery { deleteWordByDictionaryIdApiUseCase.invoke("dict-1") } returns successResponse
        coEvery { deleteDictionaryApiUseCase.invoke("dict-1") } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { deleteDictionaryUseCase.invoke("dict-1") }
        // A tombstoned dictionary is never uploaded or word-scanned.
        coVerify(exactly = 0) { saveDictionaryApiUseCase.invoke(any()) }
        coVerify(exactly = 0) { getWordsByDictionaryUseCase.invoke("dict-1") }
    }

    @Test
    fun `invoke keeps the dictionary tombstone when the remote delete fails`() = runTest {
        val tombstoned = testDictionary(dictionaryId = "dict-1", isDeleted = true)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(tombstoned)
        coEvery { deleteWordByDictionaryIdApiUseCase.invoke("dict-1") } returns successResponse
        coEvery { deleteDictionaryApiUseCase.invoke("dict-1") } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { deleteDictionaryUseCase.invoke(any()) }
    }

    @Test
    fun `invoke deletes a tombstoned word remotely then hard-deletes it locally`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isDeleted = true))
        coEvery { deleteWordApiUseCase.invoke("word-1") } returns successResponse

        useCase.invoke()

        coVerify(exactly = 1) { deleteWordUseCase.invoke("word-1") }
        // A tombstoned word is never uploaded, even though it is unsynced.
        coVerify(exactly = 0) { saveWordApiUseCase.invoke(any()) }
    }

    @Test
    fun `invoke keeps the word tombstone when the remote delete fails`() = runTest {
        val dictionary = testDictionary(dictionaryId = "dict-1", isSynced = true)
        coEvery { getDictionariesByUserUseCase.invoke(SUBJECT) } returns listOf(dictionary)
        coEvery { getWordsByDictionaryUseCase.invoke("dict-1") } returns
            listOf(testWord(wordId = "word-1", isDeleted = true))
        coEvery { deleteWordApiUseCase.invoke("word-1") } returns failureResponse

        useCase.invoke()

        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    // endregion

    private companion object {
        const val SUBJECT = "e7c1f0e2-subject"
    }
}
