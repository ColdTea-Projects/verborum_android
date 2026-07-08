package de.coldtea.verborum.core

import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule

/**
 * Base class for all unit tests in the project.
 *
 * Provides:
 * - [MainDispatcherRule] that replaces [kotlinx.coroutines.Dispatchers.Main] with a
 *   [kotlinx.coroutines.test.TestDispatcher], which makes [androidx.lifecycle.ViewModel.viewModelScope]
 *   and any coroutine launched on Main fully controllable in tests.
 * - MockK initialisation via [MockKAnnotations.init] so that fields annotated with
 *   [@MockK][io.mockk.MockK], [@SpyK][io.mockk.SpyK], or [@RelaxedMockK][io.mockk.RelaxedMockK]
 *   are wired up automatically before every test.
 * - Global unmock in [tearDown] to avoid state leaking between test classes.
 *
 * Usage — ViewModel:
 * ```
 * class DictionaryListViewModelTest : BaseTest() {
 *
 *     @MockK lateinit var dictionaryService: DictionaryService
 *     @MockK lateinit var wordService: WordService
 *
 *     private lateinit var viewModel: DictionaryListViewModel
 *
 *     override fun setUp() {
 *         super.setUp()
 *         viewModel = DictionaryListViewModel(dictionaryService, wordService, ...)
 *     }
 *
 *     @Test
 *     fun `initial state is empty list`() = runTest {
 *         assertEquals(emptyList(), viewModel.dictionaries.value)
 *     }
 * }
 * ```
 *
 * Usage — Service / UseCase:
 * ```
 * class WordServiceTest : BaseTest() {
 *
 *     @MockK lateinit var observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase
 *
 *     private lateinit var wordService: WordService
 *
 *     override fun setUp() {
 *         super.setUp()
 *         wordService = WordService(observeWordsByDictionaryUseCase, ...)
 *     }
 *
 *     @Test
 *     fun `observeWordsByDictionary maps domain model to UI model`() = runTest {
 *         val dictionaryId = "dict-1"
 *         every { observeWordsByDictionaryUseCase(dictionaryId) } returns flowOf(listOf(testWord))
 *
 *         wordService.observeWordsByDictionary(dictionaryId).test {
 *             assertEquals(listOf(testWordUi), awaitItem())
 *             cancelAndIgnoreRemainingEvents()
 *         }
 *     }
 * }
 * ```
 *
 * Flow assertions in the examples above use the Turbine library (`app.cash.turbine:turbine`).
 * Add it as a `testImplementation` dependency if you need it; it is not bundled here to keep
 * the dependency surface minimal.
 */
abstract class BaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    /**
     * The [TestDispatcher] backing [mainDispatcherRule].
     * Pass it to [kotlinx.coroutines.test.runTest] when you need the test coroutine scope to share
     * the same scheduler (required for [kotlinx.coroutines.test.StandardTestDispatcher]).
     */
    protected val testDispatcher: TestDispatcher
        get() = mainDispatcherRule.testDispatcher

    /**
     * Initialises all MockK annotations ([@MockK][io.mockk.MockK],
     * [@SpyK][io.mockk.SpyK], [@RelaxedMockK][io.mockk.RelaxedMockK]) declared in the subclass.
     *
     * [relaxUnitFun] is `true` by default so that Unit-returning functions do not need explicit
     * stubbing unless the test cares about their behaviour.
     *
     * Override to add extra setup — remember to call `super.setUp()` first.
     */
    @Before
    open fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    /**
     * Clears all MockK state after every test to prevent cross-test contamination.
     */
    @After
    open fun tearDown() {
        unmockkAll()
    }
}
