package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.testWordUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails.model.DictionaryDetailState
import de.coldtea.verborum.core.BaseTest
import io.mockk.coVerify
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

    // region cleanWords

    @Test
    fun `cleanWords delegates to WordService with the dictionary id when state is Success`() = runTest {
        val dictionaryId = "dict-1"

        every { dictionaryService.observeDictionary(dictionaryId) } returns flowOf(
            testDictionaryUi(dictionaryId = dictionaryId)
        )
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(
            listOf(testWordUi(dictionaryId = dictionaryId))
        )
        viewModel.init(dictionaryId)

        viewModel.cleanWords()

        coVerify(exactly = 1) { wordService.cleanWordsInDictionary(dictionaryId) }
    }

    @Test
    fun `cleanWords does nothing when state is not Success`() = runTest {
        // ViewModel is still in Loading state — init() was never called.
        viewModel.cleanWords()

        coVerify(exactly = 0) { wordService.cleanWordsInDictionary(any()) }
    }

    // endregion
}
