package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.ui.model.SupportedLanguage
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.DictionaryListViewModel
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryListState
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionarySort
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryListViewModelTest : BaseTest() {

    @MockK
    private lateinit var dictionaryService: DictionaryService

    @MockK
    private lateinit var wordService: WordService

    @MockK
    private lateinit var syncService: SyncService

    override fun setUp() {
        super.setUp()
        // syncDictionaries() has an inferred non-Unit return type (try/catch expression),
        // so relaxUnitFun does not answer it — stub it once for every test.
        coEvery { syncService.syncDictionaries() } returns Unit
        // Word counts merge into the dictionaries; default to none unless a test overrides it.
        every { wordService.observeWordCounts() } returns flowOf(emptyMap())
    }

    /**
     * The init block observes dictionaries and syncs immediately, so the view model is
     * constructed inside each test after the observe stub is in place.
     */
    private fun buildViewModel() = DictionaryListViewModel(
        dictionaryService = dictionaryService,
        wordService = wordService,
        syncService = syncService,
    )

    // region initial state

    @Test
    fun `initial dictionariesState is Loading`() = runTest {
        // A never-emitting flow leaves the view model at its constructor-assigned initial state.
        every { dictionaryService.observeDictionaries() } returns emptyFlow()

        val viewModel = buildViewModel()

        assertEquals(DictionaryListState.Loading, viewModel.dictionariesState.first())
    }

    // endregion

    // region init

    @Test
    fun `state reflects the observed dictionaries as Success`() = runTest {
        val dictionaries = listOf(
            testDictionaryUi(dictionaryId = "dict-1", name = "German Basics"),
            testDictionaryUi(dictionaryId = "dict-2", name = "Spanish Basics"),
        )
        every { dictionaryService.observeDictionaries() } returns flowOf(dictionaries)

        val viewModel = buildViewModel()

        assertEquals(
            DictionaryListState.Success(dictionaries),
            viewModel.dictionariesState.first(),
        )
    }

    @Test
    fun `word counts are merged onto their dictionaries and default to zero`() = runTest {
        val dictionaries = listOf(
            testDictionaryUi(dictionaryId = "dict-1"),
            testDictionaryUi(dictionaryId = "dict-2"),
        )
        every { dictionaryService.observeDictionaries() } returns flowOf(dictionaries)
        every { wordService.observeWordCounts() } returns flowOf(mapOf("dict-1" to 5))

        val viewModel = buildViewModel()

        val state = viewModel.dictionariesState.first { it is DictionaryListState.Success }
                as DictionaryListState.Success
        assertEquals(5, state.dictionaries.first { it.dictionaryId == "dict-1" }.wordCount)
        assertEquals(0, state.dictionaries.first { it.dictionaryId == "dict-2" }.wordCount)
    }

    @Test
    fun `construction triggers a dictionary sync`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()

        buildViewModel()

        coVerify(exactly = 1) { syncService.syncDictionaries() }
    }

    @Test
    fun `observe error emits Failed and still syncs`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flow {
            throw RuntimeException("db error")
        }

        val viewModel = buildViewModel()

        assertEquals(DictionaryListState.Failed, viewModel.dictionariesState.first())
        coVerify(exactly = 1) { syncService.syncDictionaries() }
    }

    // endregion

    // region retry

    @Test
    fun `retry re-subscribes and can recover from Failed to Success`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flow {
            throw RuntimeException("db error")
        }
        val viewModel = buildViewModel()
        assertEquals(DictionaryListState.Failed, viewModel.dictionariesState.first())

        val dictionaries = listOf(testDictionaryUi(dictionaryId = "dict-1"))
        every { dictionaryService.observeDictionaries() } returns flowOf(dictionaries)

        viewModel.retry()

        assertEquals(
            DictionaryListState.Success(dictionaries),
            viewModel.dictionariesState.first(),
        )
    }

    @Test
    fun `retry sets state to Loading before re-subscribing`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flow {
            throw RuntimeException("db error")
        }
        val viewModel = buildViewModel()
        assertEquals(DictionaryListState.Failed, viewModel.dictionariesState.first())

        // A never-emitting flow lets us observe the Loading state retry() sets immediately.
        every { dictionaryService.observeDictionaries() } returns emptyFlow()

        viewModel.retry()

        assertTrue(viewModel.dictionariesState.first() is DictionaryListState.Loading)
    }

    // endregion

    // region filtering & sorting

    private fun DictionaryListViewModel.successNames(): List<String> =
        (dictionariesState.value as DictionaryListState.Success).dictionaries.map { it.name }

    @Test
    fun `search filters dictionaries by name case-insensitively`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flowOf(
            listOf(
                testDictionaryUi(dictionaryId = "1", name = "German Basics"),
                testDictionaryUi(dictionaryId = "2", name = "Spanish Verbs"),
            )
        )
        val viewModel = buildViewModel()

        viewModel.onSearchQueryChange("SPAN")

        assertEquals(listOf("Spanish Verbs"), viewModel.successNames())
    }

    @Test
    fun `from and to language filters keep only matching dictionaries`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flowOf(
            listOf(
                testDictionaryUi(dictionaryId = "1", name = "A", fromLang = "en", toLang = "de"),
                testDictionaryUi(dictionaryId = "2", name = "B", fromLang = "de", toLang = "en"),
                testDictionaryUi(dictionaryId = "3", name = "C", fromLang = "en", toLang = "fr"),
            )
        )
        val viewModel = buildViewModel()

        viewModel.onFromFilterChange(SupportedLanguage.ENGLISH)
        assertEquals(listOf("A", "C"), viewModel.successNames().sorted())

        viewModel.onToFilterChange(SupportedLanguage.FRENCH)
        assertEquals(listOf("C"), viewModel.successNames())
    }

    @Test
    fun `default sort is newest first`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flowOf(
            listOf(
                testDictionaryUi(dictionaryId = "1", name = "Old", createdAt = 100L),
                testDictionaryUi(dictionaryId = "2", name = "New", createdAt = 200L),
            )
        )
        val viewModel = buildViewModel()

        assertEquals(listOf("New", "Old"), viewModel.successNames())
    }

    @Test
    fun `sort by oldest first reverses the order`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flowOf(
            listOf(
                testDictionaryUi(dictionaryId = "1", name = "Old", createdAt = 100L),
                testDictionaryUi(dictionaryId = "2", name = "New", createdAt = 200L),
            )
        )
        val viewModel = buildViewModel()

        viewModel.onSortChange(DictionarySort.OLDEST)

        assertEquals(listOf("Old", "New"), viewModel.successNames())
    }

    @Test
    fun `sort by name ascending orders alphabetically`() = runTest {
        every { dictionaryService.observeDictionaries() } returns flowOf(
            listOf(
                testDictionaryUi(dictionaryId = "1", name = "Zebra", createdAt = 200L),
                testDictionaryUi(dictionaryId = "2", name = "apple", createdAt = 100L),
            )
        )
        val viewModel = buildViewModel()

        viewModel.onSortChange(DictionarySort.NAME_ASC)

        assertEquals(listOf("apple", "Zebra"), viewModel.successNames())
    }

    @Test
    fun `toggleSearch expands, then collapsing clears the query`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()
        val viewModel = buildViewModel()

        viewModel.toggleSearch()
        assertTrue(viewModel.searchExpanded.value)

        viewModel.onSearchQueryChange("hello")
        viewModel.toggleSearch()

        assertFalse(viewModel.searchExpanded.value)
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `clearFilters resets query, language filters, sort, and search expansion`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()
        val viewModel = buildViewModel()
        viewModel.toggleSearch()
        viewModel.onSearchQueryChange("x")
        viewModel.onFromFilterChange(SupportedLanguage.GERMAN)
        viewModel.onToFilterChange(SupportedLanguage.ENGLISH)
        viewModel.onSortChange(DictionarySort.NAME_ASC)

        viewModel.clearFilters()

        assertEquals("", viewModel.searchQuery.value)
        assertNull(viewModel.fromFilter.value)
        assertNull(viewModel.toFilter.value)
        assertEquals(DictionarySort.NEWEST, viewModel.sortOrder.value)
        assertFalse(viewModel.searchExpanded.value)
    }

    // endregion

    // region deleteDictionary

    @Test
    fun `deleteDictionary tombstones first then cleans words then deletes the dictionary`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()
        val viewModel = buildViewModel()

        viewModel.deleteDictionary("dict-1")

        coVerifyOrder {
            dictionaryService.markDictionaryDeleted("dict-1")
            wordService.cleanWordsInDictionary("dict-1")
            dictionaryService.deleteDictionary("dict-1")
        }
    }

    // endregion
}
