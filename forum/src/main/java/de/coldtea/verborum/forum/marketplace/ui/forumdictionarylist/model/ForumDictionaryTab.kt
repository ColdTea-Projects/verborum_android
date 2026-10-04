package de.coldtea.verborum.forum.marketplace.ui.forumdictionarylist.model

import androidx.annotation.StringRes
import de.coldtea.verborum.forum.common.utils.ResStrings
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingOrder

/** The Forum list's tabs, in display order. Both show the same filter, sorted differently. */
enum class ForumDictionaryTab(
    @StringRes val labelRes: Int,
    val order: MarketplaceListingOrder,
) {
    POPULAR(ResStrings.forumTabPopular, MarketplaceListingOrder.POPULAR),
    FRESH(ResStrings.forumTabFresh, MarketplaceListingOrder.NEWEST),
}
