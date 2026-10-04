package de.coldtea.verborum.forum.marketplace.data.api.model

import de.coldtea.verborum.core.extensions.json
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalSerializationApi::class)
class MarketplaceResponseConversionTest {

    private fun listing(
        publishedAt: String? = "2026-07-19T17:01:21.303971Z",
        publisherName: String? = "Anna Bauer",
        tags: List<String> = emptyList(),
    ) = MarketplaceListingResponse(
        dictionaryId = "00000006-0000-4000-8000-000000000000",
        publisherId = "765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6",
        publisherName = publisherName,
        name = "Polish → Ukrainian",
        fromLang = "PL",
        toLang = "UK",
        tags = tags,
        importCount = 7,
        publishedAt = publishedAt,
    )

    // region wire format
    @Test
    fun `the guide's page example deserialises into the envelope`() {
        val body = """
            {"items":[{"dictionaryId":"00000006-0000-4000-8000-000000000000",
              "publisherId":"765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6","publisherName":"Anna Bauer",
              "name":"Polish → Ukrainian","fromLang":"PL","toLang":"UK","tags":["grammar","travel"],
              "importCount":7,"publishedAt":"2026-07-19T17:01:21.303971Z"}],
             "page":0,"size":20,"hasNext":true,"addedLater":"ignored"}
        """.trimIndent()

        val page = json.decodeFromString<MarketplacePageResponse>(body)

        assertEquals(listing(tags = listOf("grammar", "travel")), page.items.single())
        assertEquals(20, page.size)
        assertTrue(page.hasNext)
    }
    // endregion

    // region listing
    @Test
    fun `listing converts the contract fields and parses publishedAt as UTC`() {
        val result = listing().convertToListing()

        assertEquals("00000006-0000-4000-8000-000000000000", result.dictionaryId)
        assertEquals("765a81ed-2612-4dcc-bf7a-3d5fecf3d0d6", result.publisherId)
        assertEquals("Anna Bauer", result.publisherName)
        assertEquals("PL", result.fromLang)
        assertEquals("UK", result.toLang)
        assertEquals(7, result.importCount)
        assertEquals(1_784_480_481_303L, result.publishedAt)
    }

    @Test
    fun `a blank publisher name reads as unknown`() {
        assertNull(listing(publisherName = " ").convertToListing().publisherName)
    }

    @Test
    fun `listing normalises tag codes to trimmed, lowercase and distinct`() {
        val result = listing(tags = listOf(" A1 ", "food_drink", "a1", "", "FOOD_DRINK")).convertToListing()

        assertEquals(listOf("a1", "food_drink"), result.tags)
    }

    @Test
    fun `listing with a missing or malformed publishedAt falls back to 0`() {
        assertEquals(0L, listing(publishedAt = null).convertToListing().publishedAt)
        assertEquals(0L, listing(publishedAt = "yesterday").convertToListing().publishedAt)
    }
    // endregion

    // region page
    @Test
    fun `page converts its listings, de-duplicated by id, and carries hasNext`() {
        val result = MarketplacePageResponse(
            items = listOf(listing(), listing(), listing().copy(dictionaryId = "other")),
            page = 1,
            size = 3,
            hasNext = true,
        ).convertToListingPage()

        assertEquals(listOf("00000006-0000-4000-8000-000000000000", "other"), result.listings.map { it.dictionaryId })
        assertEquals(1, result.page)
        assertTrue(result.hasMore)
    }

    @Test
    fun `the last page has no more`() {
        assertFalse(MarketplacePageResponse(page = 2, size = 3, hasNext = false).convertToListingPage().hasMore)
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
