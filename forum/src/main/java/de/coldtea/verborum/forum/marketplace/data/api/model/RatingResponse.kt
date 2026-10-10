package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The caller's own rating of a dictionary, as `GET …/{id}/rating` returns it (guide §5.6). */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RatingResponse(
    @SerialName("dictionaryId")
    val dictionaryId: String? = null,
    @SerialName("stars")
    val stars: Int,
    // ISO-8601 UTC.
    @SerialName("ratedAt")
    val ratedAt: String? = null,
)
