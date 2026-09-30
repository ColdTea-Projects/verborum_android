package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `each filter change reloads the list with the new filter`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.onFromFilterChange(SupportedLanguage.ENGLISH)
        viewModel.onToFilterChange(SupportedLanguage.GERMAN)

        verify { marketplaceService.getDictionaries(ForumDictionaryFilter(fromLanguage = SupportedLanguage.ENGLISH)) }
        verify {
            marketplaceService.getDictionaries(
                ForumDictionaryFilter(fromLanguage = SupportedLanguage.ENGLISH, toLanguage = SupportedLanguage.GERMAN)
            )
        }
    }

    @Test
    fun `re-picking the current value does not reload`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.onFromFilterChange(null)

        verify(exactly = 1) { marketplaceService.getDictionaries(any()) }
    }

    @Test
    fun `toggling a tag adds it, toggling again removes it`() = runTest {
        val viewModel = viewModel()

        viewModel.onToggleTag("a1")
        viewModel.onToggleTag("travel")
        assertEquals(setOf("a1", "travel"), viewModel.filter.value.tags)

        viewModel.onToggleTag("a1")
        assertEquals(setOf("travel"), viewModel.filter.value.tags)
    }

    @Test
    fun `a user name is applied trimmed and a blank one removes the filter`() = runTest {
        val viewModel = viewModel()

        viewModel.onUserNameSearch("  anna ")
        assertEquals("anna", viewModel.filter.value.userName)

        viewModel.onUserNameSearch("   ")
        assertNull(viewModel.filter.value.userName)
    }

    @Test
    fun `searching a user name closes the input`() = runTest {
        val viewModel = viewModel()
        viewModel.toggleUserNameInput()

        viewModel.onUserNameSearch("anna")

        assertFalse(viewModel.userNameInputVisible.value)
    }

    @Test
    fun `clear resets every filter and hides the user-name input`() = runTest {
        val viewModel = viewModel()
        viewModel.onFromFilterChange(SupportedLanguage.ENGLISH)
        viewModel.onToggleTag("a1")
        viewModel.toggleUserNameInput()
        viewModel.onUserNameSearch("anna")

        viewModel.clearFilters()

        assertEquals(ForumDictionaryFilter(), viewModel.filter.value)
        assertFalse(viewModel.userNameInputVisible.value)
    }
    // endregion

    // region panel
    @Test
    fun `the magnifier and the user chip toggle their parts of the panel`() = runTest {
        val viewModel = viewModel()

        viewModel.toggleSearch()
        viewModel.toggleUserNameInput()
        assertTrue(viewModel.searchExpanded.value)
        assertTrue(viewModel.userNameInputVisible.value)

        viewModel.toggleSearch()
        viewModel.toggleUserNameInput()
        assertFalse(viewModel.searchExpanded.value)
        assertFalse(viewModel.userNameInputVisible.value)
    }
    // endregion
}
