package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body of `POST /dictionaries/{id}/tags` — a single tag (see docs/dictionary-tags-api.md §2). */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class DictionaryTagRequest(
    @SerialName("tag")
    val tag: String,
)
