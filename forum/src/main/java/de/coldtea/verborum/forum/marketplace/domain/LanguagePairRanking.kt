package de.coldtea.verborum.forum.marketplace.domain

import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import java.util.Locale

/** Picks the language pairs a user works with most, to pre-fill the Forum's filter. */
object LanguagePairRanking {

    /**
     * Up to [limit] pairs, ranked by how many dictionaries use them; a tie goes to the pair with the
     * most recent activity. Direction does not count — DE→EN and EN→DE are one pair, which is how
     * the Forum's pair filter matches too — and each pair keeps the direction of its most recently
     * used dictionary for display. A dictionary with the same language twice is ignored.
     */
    fun topPairs(dictionaries: List<DictionaryLanguages>, limit: Int): List<MarketplaceLanguagePair> =
        dictionaries
            .map { it.copy(fromLang = it.fromLang.uppercase(Locale.ROOT), toLang = it.toLang.uppercase(Locale.ROOT)) }
            .filter { it.fromLang.isNotBlank() && it.toLang.isNotBlank() && it.fromLang != it.toLang }
            .groupBy { setOf(it.fromLang, it.toLang) }
            .values
            .sortedWith(
                compareByDescending<List<DictionaryLanguages>> { it.size }
                    .thenByDescending { group -> group.maxOf(DictionaryLanguages::lastActivityAt) }
            )
            .take(limit)
            .map { group ->
                val latest = group.maxBy(DictionaryLanguages::lastActivityAt)
                MarketplaceLanguagePair(first = latest.fromLang, second = latest.toLang)
            }
}
