package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DictionaryDetailsViewModelTest : BaseTest() {

    @MockK
    private lateinit var dictionaryService: DictionaryService

    @MockK
    private lateinit var wordService: WordService

    private lateinit var viewModel: DictionaryDetailsViewModel

    override fun setUp() {
        super.setUp()
        viewModel = DictionaryDetailsViewModel(dictionaryService, wordService)
    }

    // region initial state

    @Test
    fun `initial dictionaryDetailState is Loading`() = runTest {
        assertEquals(DictionaryDetailState.Loading, viewModel.dictionaryDetailState.first())
    }

    // endregion

    // region init

    @Test
    fun `init emits Success with the observed dictionary and words`() = runTest {
        val dictionaryId = "dict-1"
        val dictionary = testDictionaryUi(dictionaryId = dictionaryId, name = "German Basics")
        val words = listOf(testWordUi(wordId = "w-1"), testWordUi(wordId = "w-2"))

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(dictionary)
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(words)

        viewModel.init(dictionaryId)

        assertEquals(
            DictionaryDetailState.Success(dictionary, words),
            viewModel.dictionaryDetailState.first(),
        )
    }

    @Test
    fun `init emits Failed when dictionary flow throws`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flow {
            throw RuntimeException("db error")
        }
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())

        viewModel.init(dictionaryId)

        assertEquals(DictionaryDetailState.Failed, viewModel.dictionaryDetailState.first())
    }

    @Test
    fun `init emits Failed when words flow throws`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(testDictionaryUi())
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flow {
            throw RuntimeException("network error")
        }

        viewModel.init(dictionaryId)

        assertEquals(DictionaryDetailState.Failed, viewModel.dictionaryDetailState.first())
    }

    @Test
    fun `init emits Deleted when the dictionary is gone`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf<DictionaryUi?>(null)
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())

        viewModel.init(dictionaryId)

        assertEquals(DictionaryDetailState.Deleted, viewModel.dictionaryDetailState.first())
    }

    @Test
    fun `init with empty word list emits Success with empty words`() = runTest {
        val dictionaryId = "dict-1"
        val dictionary = testDictionaryUi(dictionaryId = dictionaryId)

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(dictionary)
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())

        viewModel.init(dictionaryId)

        assertEquals(
            DictionaryDetailState.Success(dictionary, emptyList()),
            viewModel.dictionaryDetailState.first(),
        )
    }

    // endregion

    // region deleteWord

    @Test
    fun `deleteWord delegates to WordService with the word id`() = runTest {
        viewModel.deleteWord("word-1")

        coVerify(exactly = 1) { wordService.deleteWord("word-1") }
    }

    // endregion

    // region deleteDictionary

    private fun initSuccess(dictionaryId: String = "dict-1") {
        every { dictionaryService.observeDictionary(dictionaryId) } returns
            flowOf(testDictionaryUi(dictionaryId = dictionaryId))
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())
        viewModel.init(dictionaryId)
    }

    @Test
    fun `deleteDictionary tombstones first then cleans words then deletes the dictionary`() = runTest {
        initSuccess("dict-1")

        viewModel.deleteDictionary()

        coVerifyOrder {
            dictionaryService.markDictionaryDeleted("dict-1")
            wordService.cleanWordsInDictionary("dict-1")
            dictionaryService.deleteDictionary("dict-1")
        }
    }

    @Test
    fun `deleteDictionary does nothing when state is not Success`() = runTest {
        // init() was never called — state is still Loading.
        viewModel.deleteDictionary()

        coVerify(exactly = 0) { dictionaryService.markDictionaryDeleted(any()) }
        coVerify(exactly = 0) { dictionaryService.deleteDictionary(any()) }
    }

    // endregion
}
