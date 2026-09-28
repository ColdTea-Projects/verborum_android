package de.coldtea.verborum.forum.marketplace.data

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    @Test
    fun `getListings returns a first page in the contract envelope`() = runTest {
        val page = subject.getListings()

        assertEquals(0, page.page)
        assertEquals(MarketplaceRepository.DEFAULT_PAGE_SIZE, page.size)
        assertEquals(page.items.size.toLong(), page.totalElements)
        assertEquals(1, page.totalPages)
    }

    @Test
    fun `getListings pages with a stable order`() = runTest {
        val first = subject.getListings(page = 0, size = 3)
        val second = subject.getListings(page = 1, size = 3)

        assertEquals(3, first.items.size)
        assertTrue(first.hasMore)
        assertTrue(first.items.none { it in second.items })
    }

    @Test
    fun `listings are newest first with canonical ids and uppercase supported codes`() = runTest {
        val items = subject.getListings().items

        assertEquals(items.sortedByDescending { it.publishedAt }, items)
        assertEquals(items.size, items.map { it.dictionaryId }.toSet().size)
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
    fun `every listing's word count matches its words`() = runTest {
        subject.getListings().items.forEach { listing ->
            val words = subject.getWords(listing.dictionaryId)
            assertEquals(listing.name, listing.wordCount, words.size)
            words.forEach { word ->
                assertTrue(word.wordId, canonicalUuid.matches(word.wordId))
                assertTrue(word.word.orEmpty().startsWith("["))
            }
        }
    }

    @Test
    fun `getListing finds a known id and returns null for an unknown one`() = runTest {
        val known = subject.getListings().items.first()

        assertEquals(known, subject.getListing(known.dictionaryId))
        assertNull(subject.getListing("00000000-0000-4000-8000-000000000000"))
    }

    @Test
    fun `getWords of an unknown dictionary is empty`() = runTest {
        assertEquals(emptyList<Any>(), subject.getWords("unknown"))
    }
}
