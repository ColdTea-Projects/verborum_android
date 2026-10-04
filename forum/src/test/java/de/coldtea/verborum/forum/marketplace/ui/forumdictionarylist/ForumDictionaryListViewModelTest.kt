package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.profile.domain.ProfileService
import de.coldtea.verborum.core.profile.ui.profile.model.ProfileUi
import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumAccessState
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryFilter
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryTab
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumLanguagePair
import de.coldtea.verborum.forum.testForumDictionaryUi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
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

    @MockK
    private lateinit var profileService: ProfileService

    private val profile = MutableStateFlow<ProfileUi?>(null)

    override fun setUp() {
        super.setUp()
        every { profileService.observeProfile() } returns profile
        coEvery { marketplaceService.getDefaultFilter() } returns ForumDictionaryFilter()
    }

    private fun viewModel(): ForumDictionaryListViewModel {
        every { marketplaceService.getDictionaries(any(), any()) } returns
            flowOf(PagingData.from(listOf(testForumDictionaryUi())))
        return ForumDictionaryListViewModel(marketplaceService, profileService)
    }

    // A pager only runs while its tab's list is collected, as it is on screen.
    private fun TestScope.collect(viewModel: ForumDictionaryListViewModel, tab: ForumDictionaryTab = ForumDictionaryTab.FRESH) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.dictionaries(tab).collect {} }
    }

    // region paging
    @Test
    fun `dictionaries exposes the service's paged marketplace dictionaries`() = runTest {
        val dictionaries = listOf(testForumDictionaryUi(dictionaryId = "a"), testForumDictionaryUi(dictionaryId = "b"))
        every { marketplaceService.getDictionaries(any(), any()) } returns flowOf(PagingData.from(dictionaries))

        val viewModel = ForumDictionaryListViewModel(marketplaceService, profileService)

        // The cached stream never completes, so snapshot its first PagingData rather than the stream.
        assertEquals(dictionaries, flowOf(viewModel.dictionaries(ForumDictionaryTab.FRESH).first()).asSnapshot())
    }

    @Test
    fun `the pager is created once, however often the list is collected`() = runTest {
        val viewModel = viewModel()

        viewModel.dictionaries(ForumDictionaryTab.FRESH).first()
        viewModel.dictionaries(ForumDictionaryTab.FRESH).first()

        verify(exactly = 1) { marketplaceService.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.NEWEST) }
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
        verify { marketplaceService.getDictionaries(filter, MarketplaceListingOrder.NEWEST) }
    }

    @Test
    fun `re-applying the current filter does not reload`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.applyFilter(ForumDictionaryFilter())

        verify(exactly = 1) { marketplaceService.getDictionaries(any(), any()) }
    }

    @Test
    fun `clear resets the filter and reloads the unfiltered list`() = runTest {
        val viewModel = viewModel()
        collect(viewModel)
        viewModel.applyFilter(ForumDictionaryFilter(userName = "anna"))

        viewModel.clearFilters()

        assertEquals(ForumDictionaryFilter(), viewModel.filter.value)
        verify(exactly = 2) { marketplaceService.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.NEWEST) }
    }
    // endregion

    // region default filter & tabs
    @Test
    fun `the lists open with the user's own language pairs`() = runTest {
        val default = ForumDictionaryFilter(
            languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.ENGLISH)),
        )
        coEvery { marketplaceService.getDefaultFilter() } returns default
        val viewModel = viewModel()
        collect(viewModel)

        assertEquals(default, viewModel.filter.value)
        verify(exactly = 1) { marketplaceService.getDictionaries(default, MarketplaceListingOrder.NEWEST) }
        verify(exactly = 0) { marketplaceService.getDictionaries(ForumDictionaryFilter(), any()) }
    }

    @Test
    fun `nothing loads until the default filter is known`() = runTest {
        val pending = CompletableDeferred<ForumDictionaryFilter>()
        coEvery { marketplaceService.getDefaultFilter() } coAnswers { pending.await() }
        val viewModel = viewModel()
        collect(viewModel)

        verify(exactly = 0) { marketplaceService.getDictionaries(any(), any()) }

        pending.complete(ForumDictionaryFilter())
        verify(exactly = 1) { marketplaceService.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.NEWEST) }
    }

    @Test
    fun `a filter applied before the default arrives is kept`() = runTest {
        val pending = CompletableDeferred<ForumDictionaryFilter>()
        coEvery { marketplaceService.getDefaultFilter() } coAnswers { pending.await() }
        val viewModel = viewModel()
        val applied = ForumDictionaryFilter(tags = setOf("a1"))

        viewModel.applyFilter(applied)
        pending.complete(ForumDictionaryFilter(languagePairs = listOf(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.ENGLISH))))

        assertEquals(applied, viewModel.filter.value)
    }

    @Test
    fun `the default filter failing opens the lists unfiltered`() = runTest {
        coEvery { marketplaceService.getDefaultFilter() } throws RuntimeException("db")
        val viewModel = viewModel()
        collect(viewModel)

        verify { marketplaceService.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.NEWEST) }
    }

    @Test
    fun `Popular is the first tab and reads the popular order`() = runTest {
        val viewModel = viewModel()
        collect(viewModel, ForumDictionaryTab.POPULAR)

        assertEquals(ForumDictionaryTab.POPULAR, viewModel.selectedTab.value)
        verify { marketplaceService.getDictionaries(ForumDictionaryFilter(), MarketplaceListingOrder.POPULAR) }
    }

    @Test
    fun `a filter applies to both tabs`() = runTest {
        val viewModel = viewModel()
        collect(viewModel, ForumDictionaryTab.POPULAR)
        collect(viewModel, ForumDictionaryTab.FRESH)
        val filter = ForumDictionaryFilter(tags = setOf("travel"))

        viewModel.applyFilter(filter)

        verify { marketplaceService.getDictionaries(filter, MarketplaceListingOrder.POPULAR) }
        verify { marketplaceService.getDictionaries(filter, MarketplaceListingOrder.NEWEST) }
    }

    @Test
    fun `selecting a tab is remembered`() = runTest {
        val viewModel = viewModel()

        viewModel.selectTab(ForumDictionaryTab.FRESH)

        assertEquals(ForumDictionaryTab.FRESH, viewModel.selectedTab.value)
    }
    // endregion

    // region access gate
    @Test
    fun `access is Loading until the profile is known`() = runTest {
        assertEquals(ForumAccessState.Loading, viewModel().accessState.value)
    }

    @Test
    fun `a member may enter the Forum`() = runTest {
        val viewModel = viewModel()

        profile.value = member(true)

        assertEquals(ForumAccessState.Member, viewModel.accessState.value)
    }

    @Test
    fun `a user who has not accepted the terms is blocked`() = runTest {
        val viewModel = viewModel()

        profile.value = member(false)

        assertEquals(ForumAccessState.NotMember, viewModel.accessState.value)
    }

    @Test
    fun `joining on the profile page opens the gate without a reload`() = runTest {
        val viewModel = viewModel()
        profile.value = member(false)

        profile.value = member(true)

        assertEquals(ForumAccessState.Member, viewModel.accessState.value)
    }

    @Test
    fun `a failed check with no known profile is Failed`() = runTest {
        coEvery { profileService.refreshProfile() } throws RuntimeException("offline")
        val viewModel = viewModel()

        viewModel.refreshAccess()

        assertEquals(ForumAccessState.Failed, viewModel.accessState.value)
    }

    @Test
    fun `a failed check keeps deciding by the cached profile`() = runTest {
        coEvery { profileService.refreshProfile() } throws RuntimeException("offline")
        val viewModel = viewModel()
        profile.value = member(true)

        viewModel.refreshAccess()

        assertEquals(ForumAccessState.Member, viewModel.accessState.value)
    }
    // endregion

    private fun member(accepted: Boolean) =
        ProfileUi(email = "anna@example.com", displayName = "Anna", isMarketplaceMember = accepted)
}
