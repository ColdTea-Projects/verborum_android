package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForumDictionaryFilterTest {

    @Test
    fun `the default filter is inactive and filters nothing`() {
        assertFalse(ForumDictionaryFilter().isActive)
        assertEquals(MarketplaceListingFilter(), ForumDictionaryFilter().convertToListingFilter())
    }

    @Test
    fun `any criterion makes the filter active`() {
        assertTrue(ForumDictionaryFilter(toLanguage = SupportedLanguage.JAPANESE).isActive)
        assertTrue(ForumDictionaryFilter(tags = setOf("n5")).isActive)
        assertTrue(ForumDictionaryFilter(userName = "kenji").isActive)
    }

    @Test
    fun `conversion sends uppercase language codes`() {
        val result = ForumDictionaryFilter(
            fromLanguage = SupportedLanguage.TURKISH,
            toLanguage = SupportedLanguage.ENGLISH,
            tags = setOf("food_drink"),
            userName = "elif",
        ).convertToListingFilter()

        assertEquals(
            MarketplaceListingFilter(fromLang = "TR", toLang = "EN", tags = setOf("food_drink"), publisherName = "elif"),
            result,
        )
    }
}
