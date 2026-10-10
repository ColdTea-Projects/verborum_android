package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.core.utils.ApiTimestamp
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceListing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

/**
 * One marketplace listing (`docs/marketplace-client-guide.md` §5.2). Every text field is written by
 * another user — untrusted, rendered as plain text only.
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
    // The publisher's display name; not unique.
    @SerialName("publisherName")
    val publisherName: String? = null,
    @SerialName("name")
    val name: String,
    // Uppercase, real direction.
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
    // Lowercase and sorted; [] when untagged.
    @SerialName("tags")
    val tags: List<String> = emptyList(),
    // Distinct users who imported it.
    @SerialName("importCount")
    val importCount: Int = 0,
    // The plain average of its 1–5 star ratings, one decimal; null while nobody has rated it
    // (show "No ratings yet", never 0 stars — guide §5.2).
    @SerialName("ratingAverage")
    val ratingAverage: Float? = null,
    // How many users rated it; 0 together with a null average.
    @SerialName("ratingCount")
    val ratingCount: Int = 0,
    // ISO-8601 UTC ("2026-07-19T17:01:21.303971Z"); when it (most recently) became public.
    @SerialName("publishedAt")
    val publishedAt: String? = null,
) {
    fun convertToListing() = MarketplaceListing(
        dictionaryId = dictionaryId,
        publisherId = publisherId,
        publisherName = publisherName?.takeIf(String::isNotBlank),
        name = name,
        fromLang = fromLang,
        toLang = toLang,
        importCount = importCount,
        ratingAverage = ratingAverage?.coerceIn(MIN_RATING, MAX_RATING),
        ratingCount = ratingCount.coerceAtLeast(0),
        publishedAt = ApiTimestamp.parse(publishedAt) ?: 0L,
        // Codes are lowercase on the backend (it normalises on write); normalise defensively.
        tags = tags.normaliseTags(),
    )
}

private const val MIN_RATING = 1f
private const val MAX_RATING = 5f

/** Trimmed, lowercase, non-empty and distinct — the shape the taxonomy lookup expects. */
internal fun List<String>.normaliseTags(): List<String> =
    map { it.trim().lowercase(Locale.ROOT) }
        .filter(String::isNotEmpty)
        .distinct()
