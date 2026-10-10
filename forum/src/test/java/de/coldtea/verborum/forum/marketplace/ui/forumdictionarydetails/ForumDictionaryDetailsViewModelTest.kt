package de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarydetails.model.ForumDictionaryDetailState
import de.coldtea.verborum.forum.testForumDictionaryUi
import de.coldtea.verborum.forum.testForumWordUi
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import io.mockk.every
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import org.junit.Test

class ForumDictionaryDetailsViewModelTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    private lateinit var subject: ForumDictionaryDetailsViewModel

    override fun setUp() {
        super.setUp()
        every { marketplaceService.observeDownloadedDictionaryIds() } returns flowOf(setOf("downloaded"))
        every { marketplaceService.currentUserId() } returns "me"
        subject = ForumDictionaryDetailsViewModel(marketplaceService)
    }

    @Test
    fun `starts in Loading before init`() {
        assertEquals(ForumDictionaryDetailState.Loading, subject.dictionaryDetailState.value)
    }

    @Test
    fun `init emits Success with the dictionary and its words`() = runTest {
        val dictionary = testForumDictionaryUi(dictionaryId = "d1")
        val words = listOf(testForumWordUi(wordId = "w1"), testForumWordUi(wordId = "w2"))
        coEvery { marketplaceService.getDictionary("d1") } returns dictionary
        coEvery { marketplaceService.getWords("d1") } returns words

        subject.init("d1")

        assertEquals(ForumDictionaryDetailState.Success(dictionary, words), subject.dictionaryDetailState.value)
    }

    @Test
    fun `init emits Failed for an unknown dictionary`() = runTest {
        coEvery { marketplaceService.getDictionary("missing") } returns null
        coEvery { marketplaceService.getWords("missing") } returns emptyList()

        subject.init("missing")

        assertEquals(ForumDictionaryDetailState.Failed, subject.dictionaryDetailState.value)
    }

    @Test
    fun `init emits Failed when loading the words throws`() = runTest {
        coEvery { marketplaceService.getDictionary("d1") } returns testForumDictionaryUi()
        coEvery { marketplaceService.getWords("d1") } throws RuntimeException("offline")

        subject.init("d1")

        assertEquals(ForumDictionaryDetailState.Failed, subject.dictionaryDetailState.value)
    }

    @Test
    fun `retry after a failure loads the same dictionary again`() = runTest {
        val dictionary = testForumDictionaryUi(dictionaryId = "d1")
        coEvery { marketplaceService.getDictionary("d1") } throws RuntimeException("offline") andThen dictionary
        coEvery { marketplaceService.getWords("d1") } returns emptyList()
        subject.init("d1")

        subject.retry()

        assertEquals(ForumDictionaryDetailState.Success(dictionary, emptyList()), subject.dictionaryDetailState.value)
    }

    @Test
    fun `the shown dictionary is marked downloaded when it already is`() = runTest {
        coEvery { marketplaceService.getDictionary("downloaded") } returns testForumDictionaryUi(dictionaryId = "downloaded")
        coEvery { marketplaceService.getWords("downloaded") } returns emptyList()

        subject.init("downloaded")

        val state = subject.dictionaryDetailState.value as ForumDictionaryDetailState.Success
        assertEquals(true, state.dictionary.isDownloaded)
    }

    @Test
    fun `download downloads the shown dictionary`() = runTest {
        val dictionary = testForumDictionaryUi(dictionaryId = "d1", publisherId = "anna")
        coEvery { marketplaceService.getDictionary("d1") } returns dictionary
        coEvery { marketplaceService.getWords("d1") } returns emptyList()
        coEvery { marketplaceService.downloadDictionary(any()) } returns MarketplaceImportResult.Imported
        subject.init("d1")

        subject.download()

        coVerify(exactly = 1) { marketplaceService.downloadDictionary(dictionary) }
    }
}
