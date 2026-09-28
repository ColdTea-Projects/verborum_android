package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model.ForumDictionaryListState
import de.coldtea.verborum.forum.testForumDictionaryUi
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ForumDictionaryListViewModelTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    @Test
    fun `init emits Success with the marketplace dictionaries`() = runTest {
        val dictionaries = listOf(testForumDictionaryUi(dictionaryId = "a"), testForumDictionaryUi(dictionaryId = "b"))
        coEvery { marketplaceService.getDictionaries() } returns dictionaries

        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        assertEquals(ForumDictionaryListState.Success(dictionaries), viewModel.dictionariesState.value)
    }

    @Test
    fun `init emits Failed when loading throws`() = runTest {
        coEvery { marketplaceService.getDictionaries() } throws RuntimeException("offline")

        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        assertEquals(ForumDictionaryListState.Failed, viewModel.dictionariesState.value)
    }

    @Test
    fun `retry after a failure loads again`() = runTest {
        val dictionaries = listOf(testForumDictionaryUi())
        coEvery { marketplaceService.getDictionaries() } throws RuntimeException("offline") andThen dictionaries
        val viewModel = ForumDictionaryListViewModel(marketplaceService)

        viewModel.retry()

        assertEquals(ForumDictionaryListState.Success(dictionaries), viewModel.dictionariesState.value)
    }
}
