package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import de.coldtea.verborum.forum.testForumDictionaryUi
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ForumDictionaryListViewModelTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    private fun viewModel(): ForumDictionaryListViewModel {
        every { marketplaceService.getDictionaries(any()) } returns
            flowOf(PagingData.from(listOf(testForumDictionaryUi())))
        return ForumDictionaryListViewModel(marketplaceService)
    }

    // The pager only runs while the list is collected, as it is on screen.
    private fun TestScope.collect(viewModel: ForumDictionaryListViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.dictionaries.collect {} }
    }

    // region paging
    @Test
    fun `dictionaries exposes the service's paged marketplace dictionaries`() = runTest {
        val dictionaries = listOf(testForumDictionaryUi(dictionaryId = "a"), testForumDictionaryUi(dictionaryId = "b"))
        every { marketplaceService.getDictionaries(any()) } returns flowOf(PagingData.from(dictionaries))

        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        // The cached stream never completes, so snapshot its first PagingData rather than the stream.
        assertEquals(dictionaries, flowOf(viewModel.dictionaries.first()).asSnapshot())
    }

    @Test
    fun `the pager is created once, however often the list is collected`() = runTest {
        val viewModel = viewModel()

        viewModel.dictionaries.first()
        viewModel.dictionaries.first()

        verify(exactly = 1) { marketplaceService.getDictionaries(ForumDictionaryFilter()) }
    }
    // endregion

    // region filters
    @Test
    fun `applying a filter reloads the list with it`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)
        val filter = ForumDictionaryFilter(
            languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH)),
            tags = setOf("a1"),
        )

        viewModel.applyFilter(filter)

        assertEquals(filter, viewModel.filter.value)
        verify { marketplaceService.getDictionaries(filter) }
    }

    @Test
    fun `re-applying the current filter does not reload`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.applyFilter(ForumDictionaryFilter())

        verify(exactly = 1) { marketplaceService.getDictionaries(any()) }
    }

    @Test
    fun `clear resets the filter and reloads the unfiltered list`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)
        viewModel.applyFilter(ForumDictionaryFilter(userName = "anna"))

        viewModel.clearFilters()

        assertEquals(ForumDictionaryFilter(), viewModel.filter.value)
        verify(exactly = 2) { marketplaceService.getDictionaries(ForumDictionaryFilter()) }
    }
    // endregion
}
