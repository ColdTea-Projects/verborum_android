package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Verborum's own page envelope (not Spring's `Page`) returned by every browse endpoint —
 * exactly these five fields, unknown ones ignored (`docs/marketplace-client-guide.md` §4.2).
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
    @SerialName("totalElements")
    val totalElements: Long = 0,
    @SerialName("totalPages")
    val totalPages: Int = 0,
) {
    val hasMore: Boolean get() = page + 1 < totalPages
}
