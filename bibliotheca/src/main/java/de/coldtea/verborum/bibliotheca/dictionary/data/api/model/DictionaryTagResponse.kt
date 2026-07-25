package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One tag as returned by `GET /dictionaries/{id}/tags`. The client only needs [tag] (the normalised
 * code); the rest are informational. See docs/dictionary-tags-api.md §1.
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class DictionaryTagResponse(
    @SerialName("tagId")
    val tagId: String? = null,
    @SerialName("dictionaryId")
    val dictionaryId: String? = null,
    @SerialName("tag")
    val tag: String? = null,
    @SerialName("createdAt")
    val createdAt: String? = null,
)
