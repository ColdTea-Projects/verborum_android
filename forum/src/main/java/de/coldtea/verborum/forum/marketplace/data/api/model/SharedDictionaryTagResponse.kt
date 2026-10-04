package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One tag of a shared dictionary (`GET /dictionaries/{id}/tags`); only the code matters here. */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SharedDictionaryTagResponse(
    @SerialName("tag")
    val tag: String? = null,
)
