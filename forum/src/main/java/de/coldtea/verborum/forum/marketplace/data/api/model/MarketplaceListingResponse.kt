package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One marketplace listing, shaped like ms_marketplace's listing
 * (`docs/marketplace-client-guide.md` §4.2).
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class MarketplaceListingResponse(
    @SerialName("dictionaryId")
    val dictionaryId: String,
    // The owner's JWT `sub` — an id, never shown to the user.
    @SerialName("publisherId")
    val publisherId: String,
    @SerialName("name")
    val name: String,
    // Always uppercase on the wire (guide §4.1).
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
    // Distinct users who imported it.
    @SerialName("importCount")
    val importCount: Int = 0,
    // ISO-8601 UTC ("2026-07-19T17:01:21.303971Z"); when it (most recently) became public.
    @SerialName("publishedAt")
    val publishedAt: String? = null,
    // Not in the backend contract yet (guide §9: publisher names are BL-04, ratings are not
    // designed, and listings carry no word count). Only the dummy source fills them; a real
    // response leaves them null and the UI hides what is missing.
    @SerialName("publisherName")
    val publisherName: String? = null,
    @SerialName("rating")
    val rating: Float? = null,
    @SerialName("wordCount")
    val wordCount: Int? = null,
) {
    fun convertToListing() = MarketplaceListing(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        publisherName = publisherName,
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        importCount = importCount,
        publishedAt = ApiTimestamp.parse(publishedAt) ?: 0L,
        rating = rating?.coerceIn(0f, MAX_RATING),
        wordCount = wordCount,
    )

    companion object {
        const val MAX_RATING = 5f
    }
}
