package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListingPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Verborum's own slice envelope (not Spring's `Slice`) returned by every browse endpoint —
 * exactly these four fields, unknown ones ignored. No totals: the Forum is infinite scroll
 * (`docs/marketplace-client-guide.md` §5.2).
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class MarketplacePageResponse(
    @SerialName("items")
    val items: List<MarketplaceListingResponse> = emptyList(),
    @SerialName("page")
    val page: Int = 0,
    @SerialName("size")
    val size: Int = 0,
    @SerialName("hasNext")
    val hasNext: Boolean = false,
) {
    fun convertToListingPage() = MarketplaceListingPage(
        // The order is stable, but a listing published mid-scroll can still repeat one (§5.1).
        listings = items
            .distinctBy { it.dictionaryId }
            .map(MarketplaceListingResponse::convertToListing),
        page = page,
        hasMore = hasNext,
    )
}
