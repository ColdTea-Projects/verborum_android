package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceLanguagePair
import java.util.Locale

/**
 * A language pair the Forum search matches in either direction; [first] and [second] keep the
 * order the user picked them in, for display only.
 */
data class ForumLanguagePair(
    val first: SupportedLanguage,
    val second: SupportedLanguage,
) {
    /** True for the same two languages in either order — DE–TR is the same pair as TR–DE. */
    fun isSamePairAs(other: ForumLanguagePair): Boolean =
        setOf(first, second) == setOf(other.first, other.second)

    fun convertToListingPair() = MarketplaceLanguagePair(
        // The wire's codes are uppercase (guide §4.1).
        first = first.code.uppercase(Locale.ROOT),
        second = second.code.uppercase(Locale.ROOT),
    )
}
