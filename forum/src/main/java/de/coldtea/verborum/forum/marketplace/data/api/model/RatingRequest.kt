package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body of `PUT /marketplace/dictionaries/{id}/rating`: whole stars, 1–5 (guide §5.6). */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RatingRequest(
    @SerialName("stars")
    val stars: Int,
)
