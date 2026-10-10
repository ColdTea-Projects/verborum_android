package de.coldtea.verborum.forum.marketplace.ui.model

import de.coldtea.verborum.core.BaseTest
import de.coldtea.verborum.core.ui.UiText
import de.coldtea.verborum.forum.R
import de.coldtea.verborum.forum.marketplace.domain.MarketplaceService
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceImportResult
import de.coldtea.verborum.forum.testForumDictionaryUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumDownloadsTest : BaseTest() {

    @MockK
    private lateinit var marketplaceService: MarketplaceService

    private val stored = MutableStateFlow<Set<String>>(emptySet())

    private lateinit var downloads: ForumDownloads

    override fun setUp() {
        super.setUp()
        every { marketplaceService.observeDownloadedDictionaryIds() } returns stored
        every { marketplaceService.currentUserId() } returns "me"
        downloads = ForumDownloads(marketplaceService)
    }

    @Test
    fun `the user's own listing is marked and never downloaded`() = runTest {
        val own = testForumDictionaryUi(publisherId = "me")

        assertTrue(downloads.userState.first().applyTo(own).isOwn)
        assertNull(downloads.download(downloads.userState.first().applyTo(own), onNotMember = {}))
        coVerify(exactly = 0) { marketplaceService.downloadDictionary(any()) }
    }

    @Test
    fun `a dictionary already on the device is marked downloaded`() = runTest {
        stored.value = setOf("d1")

        assertTrue(downloads.userState.first().applyTo(testForumDictionaryUi(dictionaryId = "d1")).isDownloaded)
    }

    @Test
    fun `a successful download is marked at once and reported`() = runTest {
        coEvery { marketplaceService.downloadDictionary(any()) } returns MarketplaceImportResult.Imported

        val message = downloads.download(testForumDictionaryUi(dictionaryId = "d1"), onNotMember = {})

        assertEquals(UiText.Resource(R.string.forumDownloadSucceeded), message)
        assertTrue("d1" in downloads.userState.first().downloadedIds)
    }

    @Test
    fun `a second tap while the first is in flight does nothing`() = runTest {
        val pending = CompletableDeferred<MarketplaceImportResult>()
        coEvery { marketplaceService.downloadDictionary(any()) } coAnswers { pending.await() }
        val dictionary = testForumDictionaryUi(dictionaryId = "d1")

        val first = launch { downloads.download(dictionary, onNotMember = {}) }
        testScheduler.runCurrent()
        assertTrue("d1" in downloads.userState.first().downloadingIds)
        assertNull(downloads.download(dictionary, onNotMember = {}))

        pending.complete(MarketplaceImportResult.Imported)
        first.join()
        assertFalse("d1" in downloads.userState.first().downloadingIds)
        coVerify(exactly = 1) { marketplaceService.downloadDictionary(any()) }
    }

    @Test
    fun `an unavailable dictionary is dropped from view`() = runTest {
        coEvery { marketplaceService.downloadDictionary(any()) } returns MarketplaceImportResult.Unavailable

        val message = downloads.download(testForumDictionaryUi(dictionaryId = "gone"), onNotMember = {})

        assertEquals(UiText.Resource(R.string.forumDownloadUnavailable), message)
        assertTrue("gone" in downloads.userState.first().unavailableIds)
    }

    @Test
    fun `a refused member re-checks access`() = runTest {
        coEvery { marketplaceService.downloadDictionary(any()) } returns MarketplaceImportResult.NotMember
        var rechecked = false

        downloads.download(testForumDictionaryUi(), onNotMember = { rechecked = true })

        assertTrue(rechecked)
    }
}
