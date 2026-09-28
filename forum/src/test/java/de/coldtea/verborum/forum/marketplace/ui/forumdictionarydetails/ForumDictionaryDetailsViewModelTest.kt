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
import org.junit.Test

class ForumDictionaryDetailsViewModelTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    private lateinit var subject: ForumDictionaryDetailsViewModel

    override fun setUp() {
        super.setUp()
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
}
