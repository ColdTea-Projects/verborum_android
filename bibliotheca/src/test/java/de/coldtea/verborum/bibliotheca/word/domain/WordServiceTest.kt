package de.coldtea.verborum.bibliotheca.word.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.testWord
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.MarkWordDeletedUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordCountsUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import retrofit2.Response
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WordServiceTest : BaseTest() {

    @MockK
    private lateinit var observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase

    @MockK
    private lateinit var observeWordCountsUseCase: ObserveWordCountsUseCase

    // invoke returns Response<Unit> — not covered by relaxUnitFun, so relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase

    // invoke returns Response<Unit> — stubbed per test with coEvery.
    @MockK
    private lateinit var deleteWordApiUseCase: DeleteWordApiUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var deleteWordUseCase: DeleteWordUseCase

    // invoke returns the repository result (non-Unit) — relax fully for verify-only use.
    @MockK(relaxed = true)
    private lateinit var markWordDeletedUseCase: MarkWordDeletedUseCase

    // invoke returns Word — stubbed per test with coEvery.
    @MockK
    private lateinit var getWordUseCase: GetWordUseCase

    @MockK
    private lateinit var saveWordUseCase: SaveWordUseCase

    private val successResponse = mockk<Response<Unit>> { every { isSuccessful } returns true }
    private val failureResponse = mockk<Response<Unit>> { every { isSuccessful } returns false }

    @MockK
    private lateinit var syncService: SyncService

    @MockK
    private lateinit var uploadService: UploadService

    private lateinit var wordService: WordService

    override fun setUp() {
        super.setUp()
        wordService = WordService(
            observeWordsByDictionaryUseCase = observeWordsByDictionaryUseCase,
            observeWordCountsUseCase = observeWordCountsUseCase,
            deleteWordByDictionaryIdApiUseCase = deleteWordByDictionaryIdApiUseCase,
            deleteWordByDictionaryIdUseCase = deleteWordByDictionaryIdUseCase,
            deleteWordApiUseCase = deleteWordApiUseCase,
            deleteWordUseCase = deleteWordUseCase,
            markWordDeletedUseCase = markWordDeletedUseCase,
            getWordUseCase = getWordUseCase,
            saveWordUseCase = saveWordUseCase,
            syncService = syncService,
            uploadService = uploadService,
        )
    }

    // region observeWordsByDictionary

    @Test
    fun `observeWordsByDictionary maps domain Word to WordUi`() = runTest {
        val dictionaryId = "dict-1"
        val word = testWord(wordId = "w-1", dictionaryId = dictionaryId)
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flowOf(listOf(word))

        val result = wordService.observeWordsByDictionary(dictionaryId).first()

        assertEquals(listOf(word.convertToUi()), result)
    }

    @Test
    fun `observeWordsByDictionary maps multiple words preserving order`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(
            testWord(wordId = "w-1", word = "apple"),
            testWord(wordId = "w-2", word = "bread"),
            testWord(wordId = "w-3", word = "coffee"),
        )
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flowOf(words)

        val result = wordService.observeWordsByDictionary(dictionaryId).first()

        assertEquals(words.map(Word::convertToUi), result)
    }

    @Test
    fun `observeWordsByDictionary suppresses duplicate emissions`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(testWord(wordId = "w-1"))
        // emit the exact same list twice — only one downstream emission expected
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flow {
            emit(words)
            emit(words)
        }

        val emissions = wordService.observeWordsByDictionary(dictionaryId).toList()

        assertEquals(1, emissions.size)
    }

    @Test
    fun `observeWordsByDictionary emits both events when list content changes`() = runTest {
        val dictionaryId = "dict-1"
        val first = listOf(testWord(wordId = "w-1", level = 0))
        val second = listOf(testWord(wordId = "w-1", level = 1))
        every { observeWordsByDictionaryUseCase.invoke(dictionaryId) } returns flow {
            emit(first)
            emit(second)
        }

        val emissions = wordService.observeWordsByDictionary(dictionaryId).toList()

        assertEquals(2, emissions.size)
        assertEquals(0, emissions[0].first().level)
        assertEquals(1, emissions[1].first().level)
    }

    // endregion

    // region observeWordCounts

    @Test
    fun `observeWordCounts delegates to the use case`() = runTest {
        val counts = mapOf("dict-1" to 3, "dict-2" to 7)
        every { observeWordCountsUseCase.invoke() } returns flowOf(counts)

        val result = wordService.observeWordCounts().first()

        assertEquals(counts, result)
    }

    // endregion

    // region getWord

    @Test
    fun `getWord maps the domain word to WordUi`() = runTest {
        val word = testWord(wordId = "word-1")
        coEvery { getWordUseCase.invoke("word-1") } returns word

        val result = wordService.getWord("word-1")

        assertEquals(word.convertToUi(), result)
    }

    // endregion

    // region saveWord

    @Test
    fun `saveWord delegates to SaveWordUseCase`() = runTest {
        val word = testWord()

        wordService.saveWord(word)

        coVerify(exactly = 1) { saveWordUseCase.invoke(word) }
    }

    // endregion

    // region deleteWord

    @Test
    fun `deleteWord tombstones first then deletes remotely and locally on success`() = runTest {
        coEvery { deleteWordApiUseCase.invoke("word-1") } returns successResponse

        wordService.deleteWord("word-1")

        coVerifyOrder {
            markWordDeletedUseCase.invoke("word-1")
            deleteWordApiUseCase.invoke("word-1")
            deleteWordUseCase.invoke("word-1")
        }
    }

    @Test
    fun `deleteWord keeps the tombstone when the api delete fails`() = runTest {
        coEvery { deleteWordApiUseCase.invoke("word-1") } returns failureResponse

        wordService.deleteWord("word-1")

        coVerify(exactly = 1) { markWordDeletedUseCase.invoke("word-1") }
        coVerify(exactly = 0) { deleteWordUseCase.invoke(any()) }
    }

    // endregion

    // region cleanWordsInDictionary

    @Test
    fun `cleanWordsInDictionary deletes locally only after the api confirms`() = runTest {
        coEvery { deleteWordByDictionaryIdApiUseCase.invoke("dict-1") } returns successResponse

        wordService.cleanWordsInDictionary("dict-1")

        coVerify(exactly = 1) { deleteWordByDictionaryIdApiUseCase.invoke("dict-1") }
        coVerify(exactly = 1) { deleteWordByDictionaryIdUseCase.invoke("dict-1") }
    }

    @Test
    fun `cleanWordsInDictionary skips the local delete when the api fails`() = runTest {
        coEvery { deleteWordByDictionaryIdApiUseCase.invoke("dict-1") } returns failureResponse

        wordService.cleanWordsInDictionary("dict-1")

        coVerify(exactly = 0) { deleteWordByDictionaryIdUseCase.invoke(any()) }
    }

    // endregion
}
