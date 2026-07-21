package de.coldtea.verborum.bibliotheca.word.ui.selfpractice

import de.coldtea.verborum.bibliotheca.common.domain.PendingUploadSyncTrigger
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.selfpractice.model.SelfPracticeState
import de.coldtea.verborum.core.BaseTest
import io.mockk.impl.annotations.MockK
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelfPracticeViewModelTest : BaseTest() {

    @MockK
    private lateinit var dictionaryService: DictionaryService

    @MockK
    private lateinit var wordService: WordService

    // pause()/resume() return Unit — covered by relaxUnitFun.
    @MockK
    private lateinit var pendingUploadSyncTrigger: PendingUploadSyncTrigger

    private lateinit var viewModel: SelfPracticeViewModel

    override fun setUp() {
        super.setUp()
        viewModel = SelfPracticeViewModel(dictionaryService, wordService, pendingUploadSyncTrigger)
    }

    // region initial state

    @Test
    fun `initial selfPracticeState is Loading`() = runTest {
        assertEquals(SelfPracticeState.Loading, viewModel.selfPracticeState.first())
    }

    // endregion

    // region init

    @Test
    fun `init emits Success when both flows emit`() = runTest {
        val dictionaryId = "dict-1"
        val dictionary = testDictionaryUi(name = "German Basics")
        val words = listOf(testWordUi(wordId = "w-1"), testWordUi(wordId = "w-2"))

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(dictionary)
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(words)

        viewModel.init(dictionaryId)

        val successState = viewModel.selfPracticeState.first() as SelfPracticeState.Success
        assertEquals("German Basics", successState.dictionaryName)
        assertEquals(words, successState.wordsUi)
    }

    @Test
    fun `init emits Failed when dictionary flow throws`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flow {
            throw RuntimeException("network error")
        }
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())

        viewModel.init(dictionaryId)

        assertEquals(SelfPracticeState.Failed, viewModel.selfPracticeState.first())
    }

    @Test
    fun `init emits Failed when words flow throws`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(testDictionaryUi())
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flow {
            throw RuntimeException("db error")
        }

        viewModel.init(dictionaryId)

        assertEquals(SelfPracticeState.Failed, viewModel.selfPracticeState.first())
    }

    @Test
    fun `init with empty word list emits Success with empty list`() = runTest {
        val dictionaryId = "dict-1"
        val dictionary = testDictionaryUi()

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(dictionary)
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())

        viewModel.init(dictionaryId)

        val successState = viewModel.selfPracticeState.first() as SelfPracticeState.Success
        assertEquals(emptyList<Nothing>(), successState.wordsUi)
    }

    // endregion

    // region onProgressUpdated

    @Test
    fun `onProgressUpdated saves word with updated level`() = runTest {
        val dictionaryId = "dict-1"
        val wordId = "w-1"
        val originalWordUi = testWordUi(wordId = wordId, level = 0)

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(testDictionaryUi())
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(listOf(originalWordUi))

        viewModel.init(dictionaryId)

        viewModel.onProgressUpdated(wordId = wordId, progress = 4)

        val expectedWord = originalWordUi.copy(level = 4).convertToWord()
        coVerify(exactly = 1) { wordService.saveWord(expectedWord) }
    }

    @Test
    fun `onProgressUpdated with unknown wordId keeps the session and saves nothing`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(testDictionaryUi())
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(
            listOf(testWordUi(wordId = "w-1"))
        )

        viewModel.init(dictionaryId)

        viewModel.onProgressUpdated(wordId = "does-not-exist", progress = 2)

        // A stray id must not tear down an active practice session, and nothing is persisted.
        assertTrue(viewModel.selfPracticeState.first() is SelfPracticeState.Success)
        coVerify(exactly = 0) { wordService.saveWord(any()) }
    }

    @Test
    fun `onProgressUpdated keeps the session on a failed save`() = runTest {
        // A failed save must not tear down the session (the failure surfaces via snackbar, which
        // is a replay-0 SharedFlow and so cannot be asserted post-hoc — the observable contract
        // here is: the save was attempted and the state stays Success rather than Failed).
        val dictionaryId = "dict-1"
        val wordId = "w-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(testDictionaryUi())
        every { wordService.observeWordsByDictionary(dictionaryId) } returns
            flowOf(listOf(testWordUi(wordId = wordId)))
        coEvery { wordService.saveWord(any()) } throws RuntimeException("db error")

        viewModel.init(dictionaryId)
        viewModel.onProgressUpdated(wordId = wordId, progress = 4)

        coVerify(exactly = 1) { wordService.saveWord(any()) }
        assertTrue(viewModel.selfPracticeState.first() is SelfPracticeState.Success)
    }

    @Test
    fun `onProgressUpdated does nothing when state is not Success`() = runTest {
        // ViewModel is in Loading state — init() was never called.
        viewModel.onProgressUpdated(wordId = "w-1", progress = 3)

        coVerify(exactly = 0) { wordService.saveWord(any()) }
    }

    // endregion
}
