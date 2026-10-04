package de.coldtea.verborum.forum.marketplace.domain

import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguagePairRankingTest {

    private fun languages(from: String, to: String, at: Long = 0L) = DictionaryLanguages(from, to, at)

    @Test
    fun `ranks pairs by dictionary count, ignoring direction`() {
        val result = LanguagePairRanking.topPairs(
            listOf(
                languages("FR", "EN"),
                languages("DE", "EN"), languages("EN", "DE"), languages("DE", "EN"),
                languages("TR", "EN"), languages("EN", "TR"),
                languages("JA", "EN"),
            ),
            limit = 3,
        ).map { setOf(it.first, it.second) }

        assertEquals(listOf(setOf("DE", "EN"), setOf("TR", "EN")), result.take(2))
        assertEquals(3, result.size)
    }

    @Test
    fun `a tie goes to the most recently used pair`() {
        val result = LanguagePairRanking.topPairs(
            listOf(languages("FR", "EN", at = 1), languages("JA", "EN", at = 5), languages("IT", "EN", at = 3)),
            limit = 2,
        )

        assertEquals(listOf(MarketplaceLanguagePair("JA", "EN"), MarketplaceLanguagePair("IT", "EN")), result)
    }

    @Test
    fun `a pair keeps the direction of its most recent dictionary, uppercased`() {
        val result = LanguagePairRanking.topPairs(
            listOf(languages("de", "en", at = 1), languages("en", "de", at = 9)),
            limit = 3,
        )

        assertEquals(listOf(MarketplaceLanguagePair("EN", "DE")), result)
    }

    @Test
    fun `fewer pairs than the limit returns just those, and same-language ones are ignored`() {
        val result = LanguagePairRanking.topPairs(listOf(languages("DE", "EN"), languages("EN", "en")), limit = 3)

        assertEquals(listOf(MarketplaceLanguagePair("DE", "EN")), result)
    }
}
