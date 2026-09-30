package de.coldtea.verborum.forum.marketplace.data.api.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketplaceResponseConversionTest {

    private fun listing(
        publishedAt: String? = "2026-07-19T17:01:21.303971Z",
        rating: Float? = null,
        publisherName: String? = null,
        wordCount: Int? = null,
        tags: List<String>? = null,
    ) = MarketplaceListingResponse(
        dictionaryId = "00000006-0000-4000-8000-000000000000",
        publisherId = "765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6",
        name = "Polish → Ukrainian",
        fromLang = "PL",
        toLang = "UK",
        importCount = 7,
        publishedAt = publishedAt,
        publisherName = publisherName,
        rating = rating,
        wordCount = wordCount,
        tags = tags,
    )

    // region listing
    @Test
    fun `listing converts the contract fields and parses publishedAt as UTC`() {
        val result = listing().convertToListing()

        assertEquals("00000006-0000-4000-8000-000000000000", result.dictionaryId)
        assertEquals("765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6", result.publisherId)
        assertEquals("PL", result.fromLang)
        assertEquals("UK", result.toLang)
        assertEquals(7, result.importCount)
        assertEquals(1_784_480_481_303L, result.publishedAt)
    }

    @Test
    fun `listing without the not-yet-contracted fields keeps them null`() {
        val result = listing().convertToListing()

        assertNull(result.publisherName)
        assertNull(result.rating)
        assertNull(result.wordCount)
        assertEquals(emptyList<String>(), result.tags)
    }

    @Test
    fun `listing normalises tag codes to trimmed, lowercase and distinct`() {
        val result = listing(tags = listOf(" A1 ", "food_drink", "a1", "", "FOOD_DRINK")).convertToListing()

        assertEquals(listOf("a1", "food_drink"), result.tags)
    }

    @Test
    fun `listing clamps an out-of-range rating into 0 to 5`() {
        assertEquals(5f, listing(rating = 7.3f).convertToListing().rating)
        assertEquals(0f, listing(rating = -1f).convertToListing().rating)
    }

    @Test
    fun `listing with a missing or malformed publishedAt falls back to 0`() {
        assertEquals(0L, listing(publishedAt = null).convertToListing().publishedAt)
        assertEquals(0L, listing(publishedAt = "yesterday").convertToListing().publishedAt)
    }

    @Test
    fun `page hasMore is true only before the last page`() {
        assertTrue(MarketplacePageResponse(page = 0, totalPages = 3).hasMore)
        assertFalse(MarketplacePageResponse(page = 2, totalPages = 3).hasMore)
        assertFalse(MarketplacePageResponse(page = 0, totalPages = 0).hasMore)
    }
    // endregion

    // region word
    private fun word(word: String?, translation: String? = "[\"kaufen\"]") =
        MarketplaceWordResponse(
            wordId = "wordId",
            dictionaryId = "dictionaryId",
            word = word,
            translation = translation,
        )

    @Test
    fun `word parses the JSON-array surfaces columns`() {
        val result = word("[\"buy\",\"purchase\"]").convertToWord()

        assertEquals(listOf("buy", "purchase"), result.surfaces)
        assertEquals(listOf("kaufen"), result.translations)
    }

    @Test
    fun `word keeps a non-JSON column as its raw text`() {
        assertEquals(listOf("buy"), word("buy").convertToWord().surfaces)
    }

    @Test
    fun `word drops blank alternatives and treats a missing column as empty`() {
        val result = word("[\"buy\",\" \"]", translation = null).convertToWord()

        assertEquals(listOf("buy"), result.surfaces)
        assertEquals(emptyList<String>(), result.translations)
    }
    // endregion
}
