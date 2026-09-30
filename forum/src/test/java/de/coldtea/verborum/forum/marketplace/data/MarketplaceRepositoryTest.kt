package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.core.ui.model.dictionaryTagByCode
import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.composables.MAX_VISIBLE_TAG_CHIPS
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The dummy source must look like the real wire data, or the eventual API swap changes behaviour.
 */
class MarketplaceRepositoryTest {

    private val subject = MarketplaceRepository()
    private val canonicalUuid =
        Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

    // Every listing, fetched page by page as the Forum list does.
    private suspend fun allListings() =
        (0 until MarketplaceDummyData.TOTAL_LISTINGS / MarketplaceRepository.MAX_PAGE_SIZE)
            .flatMap { subject.getListings(page = it, size = MarketplaceRepository.MAX_PAGE_SIZE).items }

    // region getListings
    @Test
    fun `getListings returns a first page in the contract envelope`() = runTest {
        val page = subject.getListings(page = 0, size = 15)

        assertEquals(0, page.page)
        assertEquals(15, page.size)
        assertEquals(15, page.items.size)
        assertEquals(MarketplaceDummyData.TOTAL_LISTINGS.toLong(), page.totalElements)
        assertEquals(MarketplaceDummyData.TOTAL_LISTINGS / 15, page.totalPages)
        assertTrue(page.hasMore)
    }

    @Test
    fun `getListings pages with a stable order`() = runTest {
        val first = subject.getListings(page = 0, size = 15)
        val second = subject.getListings(page = 1, size = 15)

        assertEquals(first, subject.getListings(page = 0, size = 15))
        assertTrue(first.items.none { it in second.items })
    }

    @Test
    fun `the last page is partial and has no more after it`() = runTest {
        val size = 7 * 6 // TOTAL_LISTINGS is not a multiple of this
        val lastPage = (MarketplaceDummyData.TOTAL_LISTINGS - 1) / size

        val page = subject.getListings(page = lastPage, size = size)

        assertEquals(MarketplaceDummyData.TOTAL_LISTINGS % size, page.items.size)
        assertFalse(page.hasMore)
        assertTrue(subject.getListings(page = lastPage + 1, size = size).items.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getListings rejects a page size the backend would reject`() = runTest {
        subject.getListings(page = 0, size = MarketplaceRepository.MAX_PAGE_SIZE + 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getListings rejects a negative page`() = runTest {
        subject.getListings(page = -1, size = 15)
    }
    // endregion

    @Test
    fun `listings repeat the templates with unique canonical ids and uppercase supported codes`() = runTest {
        val items = allListings()

        assertEquals(MarketplaceDummyData.TOTAL_LISTINGS, items.size)
        assertEquals(items.size, items.map { it.dictionaryId }.toSet().size)
        assertEquals(7, items.map { it.name }.toSet().size)
        items.forEach { listing ->
            assertTrue(listing.dictionaryId, canonicalUuid.matches(listing.dictionaryId))
            assertTrue(listing.publisherId, canonicalUuid.matches(listing.publisherId))
            listOf(listing.fromLang, listing.toLang).forEach { code ->
                assertEquals(code.uppercase(), code)
                assertNotNull(code, SupportedLanguage.fromCode(code))
            }
        }
    }

    @Test
    fun `listings stay newest first across every repetition`() = runTest {
        val publishedAt = allListings().map { ApiTimestamp.parse(it.publishedAt) }

        assertTrue(publishedAt.none { it == null })
        assertEquals(publishedAt.sortedByDescending { it }, publishedAt)
        assertEquals(publishedAt.size, publishedAt.toSet().size)
    }

    @Test
    fun `every listing's word count matches its words`() = runTest {
        val wordIds = mutableSetOf<String>()
        allListings().forEach { listing ->
            val words = subject.getWords(listing.dictionaryId)
            assertEquals(listing.name, listing.wordCount, words.size)
            words.forEach { word ->
                assertTrue(word.wordId, canonicalUuid.matches(word.wordId))
                assertTrue(word.wordId, wordIds.add(word.wordId))
                assertEquals(listing.dictionaryId, word.dictionaryId)
                assertTrue(word.word.orEmpty().startsWith("["))
            }
        }
    }

    @Test
    fun `every dummy tag is a known taxonomy code`() = runTest {
        subject.getListings().items.flatMap { it.tags.orEmpty() }.forEach { code ->
            assertNotNull(code, dictionaryTagByCode(code))
        }
    }

    @Test
    fun `dummy data covers a card over the chip limit and one without tags`() = runTest {
        val tagCounts = subject.getListings().items.map { it.tags.orEmpty().size }

        assertTrue(tagCounts.any { it > MAX_VISIBLE_TAG_CHIPS })
        assertTrue(tagCounts.any { it == 0 })
    }

    @Test
    fun `getListing finds a known id and returns null for an unknown one`() = runTest {
        val known = subject.getListings(page = 2, size = 15).items.first()

        assertEquals(known, subject.getListing(known.dictionaryId))
        assertNull(subject.getListing("00000000-0000-4000-8000-000000000000"))
    }

    @Test
    fun `getWords of an unknown dictionary is empty`() = runTest {
        assertEquals(emptyList<Any>(), subject.getWords("unknown"))
    }
}
