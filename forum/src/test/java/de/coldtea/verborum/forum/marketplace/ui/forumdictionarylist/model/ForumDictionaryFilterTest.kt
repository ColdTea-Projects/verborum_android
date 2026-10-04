package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
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
        assertTrue(
            ForumDictionaryFilter(
                languagePairs = listOf(ForumLanguagePair(SupportedLanguage.ENGLISH, SupportedLanguage.JAPANESE)),
            ).isActive
        )
        assertTrue(ForumDictionaryFilter(tags = setOf("n5")).isActive)
        assertTrue(ForumDictionaryFilter(userName = "kenji").isActive)
    }

    @Test
    fun `a pair is the same pair in either order`() {
        val deTr = ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH)

        assertTrue(deTr.isSamePairAs(ForumLanguagePair(SupportedLanguage.TURKISH, SupportedLanguage.GERMAN)))
        assertFalse(deTr.isSamePairAs(ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.ENGLISH)))
    }

    @Test
    fun `conversion sends uppercase language codes`() {
        val result = ForumDictionaryFilter(
            languagePairs = listOf(
                ForumLanguagePair(SupportedLanguage.GERMAN, SupportedLanguage.TURKISH),
                ForumLanguagePair(SupportedLanguage.FRENCH, SupportedLanguage.ENGLISH),
            ),
            tags = setOf("food_drink"),
            userName = "elif",
        ).convertToListingFilter()

        assertEquals(
            MarketplaceListingFilter(
                languagePairs = listOf(MarketplaceLanguagePair("DE", "TR"), MarketplaceLanguagePair("FR", "EN")),
                tags = setOf("food_drink"),
                publisherName = "elif",
            ),
            result,
        )
    }
}
