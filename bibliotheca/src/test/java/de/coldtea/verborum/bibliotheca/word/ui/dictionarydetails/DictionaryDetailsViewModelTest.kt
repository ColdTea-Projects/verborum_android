package de.coldtea.verborum.bibliotheca.word.ui.dictionarydetails

import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        // Distractor pool for the test gate; tests that care about it override this stub.
        every { wordService.observeWordsInLanguagePair(any()) } returns flowOf(emptyList())
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
            // The default stub leaves the language pair empty, so a test cannot be built yet.
            DictionaryDetailState.Success(
                dictionaryUi = dictionary,
                wordsUi = words,
                canSelfPractice = true,
                canTest = false,
            ),
            viewModel.dictionaryDetailState.first(),
        )
    }

    // region practice availability

    @Test
    fun `test is available once the language pair holds four distinct words`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(testWordUi(wordId = "w-1"))
        // Only one word here, but the pair as a whole has enough to build distractors.
        val languagePairWords = (1..4).map {
            testWordUi(wordId = "p-$it", word = "word-$it", translation = "translation-$it")
        }

        every { dictionaryService.observeDictionary(dictionaryId) } returns
            flowOf(testDictionaryUi(dictionaryId = dictionaryId))
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(words)
        every { wordService.observeWordsInLanguagePair(dictionaryId) } returns
            flowOf(languagePairWords)

        viewModel.init(dictionaryId)

        val state = viewModel.dictionaryDetailState.first() as DictionaryDetailState.Success
        assertTrue(state.canSelfPractice)
        assertTrue(state.canTest)
    }

    @Test
    fun `test stays unavailable when the language pair has only three distinct words`() = runTest {
        val dictionaryId = "dict-1"
        val words = listOf(testWordUi(wordId = "w-1"))
        val languagePairWords = (1..3).map {
            testWordUi(wordId = "p-$it", word = "word-$it", translation = "translation-$it")
        }

        every { dictionaryService.observeDictionary(dictionaryId) } returns
            flowOf(testDictionaryUi(dictionaryId = dictionaryId))
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(words)
        every { wordService.observeWordsInLanguagePair(dictionaryId) } returns
            flowOf(languagePairWords)

        viewModel.init(dictionaryId)

        val state = viewModel.dictionaryDetailState.first() as DictionaryDetailState.Success
        // Self practice only needs this dictionary's own words.
        assertTrue(state.canSelfPractice)
        assertFalse(state.canTest)
    }

    @Test
    fun `neither mode is available for an empty dictionary`() = runTest {
        val dictionaryId = "dict-1"
        val languagePairWords = (1..4).map {
            testWordUi(wordId = "p-$it", word = "word-$it", translation = "translation-$it")
        }

        every { dictionaryService.observeDictionary(dictionaryId) } returns
            flowOf(testDictionaryUi(dictionaryId = dictionaryId))
        every { wordService.observeWordsByDictionary(dictionaryId) } returns flowOf(emptyList())
        every { wordService.observeWordsInLanguagePair(dictionaryId) } returns
            flowOf(languagePairWords)

        viewModel.init(dictionaryId)

        val state = viewModel.dictionaryDetailState.first() as DictionaryDetailState.Success
        assertFalse(state.canSelfPractice)
        assertFalse(state.canTest)
    }

    // endregion

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
