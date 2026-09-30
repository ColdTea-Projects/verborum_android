package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import de.coldtea.verborum.core.ui.model.SupportedLanguage
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingFilter
import java.util.Locale

/** The Forum search panel's criteria; the default instance filters nothing. */
data class ForumDictionaryFilter(
    val fromLanguage: SupportedLanguage? = null,
    val toLanguage: SupportedLanguage? = null,
    // Tag codes.
    val tags: Set<String> = emptySet(),
    // The publisher's user name; null when not filtering by user.
    val userName: String? = null,
) {
    val isActive: Boolean
        get() = this != ForumDictionaryFilter()

    fun convertToListingFilter() = MarketplaceListingFilter(
        // The wire's codes are uppercase (guide §4.1).
        fromLang = fromLanguage?.code?.uppercase(Locale.ROOT),
        toLang = toLanguage?.code?.uppercase(Locale.ROOT),
        tags = tags,
        publisherName = userName,
    )
}
