package de.coldtea.verborum.bibliotheca.dictionary.ui

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.dictionary.domain.DictionaryService
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.testDictionaryUi
import de.coldtea.verborum.bibliotheca.word.domain.WordService
import de.coldtea.verborum.core.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `initial dictionariesState is an empty list`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()

        val viewModel = buildViewModel()

        assertEquals(emptyList<DictionaryUi>(), viewModel.dictionariesState.first())
    }

    // endregion

    // region init

    @Test
    fun `state reflects the observed dictionaries`() = runTest {
        val dictionaries = listOf(
            testDictionaryUi(dictionaryId = "dict-1", name = "German Basics"),
            testDictionaryUi(dictionaryId = "dict-2", name = "Spanish Basics"),
        )
        every { dictionaryService.observeDictionaries() } returns flowOf(dictionaries)

        val viewModel = buildViewModel()

        assertEquals(dictionaries, viewModel.dictionariesState.first())
    }

    @Test
    fun `construction triggers a dictionary sync`() = runTest {
        every { dictionaryService.observeDictionaries() } returns emptyFlow()

        buildViewModel()

        coVerify(exactly = 1) { syncService.syncDictionaries() }
    }

    @Test
    fun `observe error keeps state empty and still syncs`() = runTest {
        // The error is handled via onError, which surfaces a snackbar message.
        // (The snackbar SharedFlow has replay 0, so the emission itself cannot be
        // asserted post-hoc — the observable contract here is: state stays empty,
        // sync still runs, and no exception escapes.)
        every { dictionaryService.observeDictionaries() } returns flow {
            throw RuntimeException("db error")
        }

        val viewModel = buildViewModel()

        assertEquals(emptyList<DictionaryUi>(), viewModel.dictionariesState.first())
        coVerify(exactly = 1) { syncService.syncDictionaries() }
    }

    // endregion

    // region deleteDictionary

    @Test
    fun `deleteDictionary tombstones first then cleans words then deletes the dictionary`() = runTest {
        every { dictionaryService.observeDictionaries() } returns
            flowOf(listOf(testDictionaryUi(dictionaryId = "dict-1")))
        val viewModel = buildViewModel()

        viewModel.deleteDictionary("dict-1")

        coVerifyOrder {
            dictionaryService.markDictionaryDeleted("dict-1")
            wordService.cleanWordsInDictionary("dict-1")
            dictionaryService.deleteDictionary("dict-1")
        }
    }

    @Test
    fun `deleteDictionary failure keeps the tombstone and does not crash`() = runTest {
        // The BaseViewModel exceptionHandler logs the failure — Log must be mocked in unit tests.
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { dictionaryService.observeDictionaries() } returns flowOf(emptyList())
        coEvery { wordService.cleanWordsInDictionary("dict-1") } throws RuntimeException("api down")
        val viewModel = buildViewModel()

        viewModel.deleteDictionary("dict-1") // must not throw — handled by exceptionHandler

        // The tombstone was written before the network failure; the hard delete never ran.
        coVerify(exactly = 1) { dictionaryService.markDictionaryDeleted("dict-1") }
        coVerify(exactly = 0) { dictionaryService.deleteDictionary(any()) }
    }

    // endregion
}
