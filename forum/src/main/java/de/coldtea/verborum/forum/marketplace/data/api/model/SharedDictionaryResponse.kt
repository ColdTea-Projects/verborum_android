package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Someone else's public dictionary as ms_dictionary's `GET /dictionaries/dictionary/{id}` returns
 * it — the current name and languages, read fresh for the details screen.
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SharedDictionaryResponse(
    @SerialName("dictionaryId")
    val dictionaryId: String,
    // The owner's `sub` — the listing's publisherId.
    @SerialName("userId")
    val userId: String,
    @SerialName("name")
    val name: String,
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
    @SerialName("createdAt")
    val createdAt: String? = null,
)
