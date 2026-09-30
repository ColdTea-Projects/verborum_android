package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.testForumDictionaryUi
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ForumDictionaryListViewModelTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    @Test
    fun `dictionaries exposes the service's paged marketplace dictionaries`() = runTest {
        val dictionaries = listOf(testForumDictionaryUi(dictionaryId = "a"), testForumDictionaryUi(dictionaryId = "b"))
        every { marketplaceService.getDictionaries() } returns flowOf(PagingData.from(dictionaries))

        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        // The cached stream never completes, so snapshot its first PagingData rather than the stream.
        assertEquals(dictionaries, flowOf(viewModel.dictionaries.first()).asSnapshot())
    }

    @Test
    fun `the pager is created once, however often the list is collected`() = runTest {
        every { marketplaceService.getDictionaries() } returns flowOf(PagingData.from(listOf(testForumDictionaryUi())))
        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        viewModel.dictionaries.first()
        viewModel.dictionaries.first()

        verify(exactly = 1) { marketplaceService.getDictionaries() }
    }
}
