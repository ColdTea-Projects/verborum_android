package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class DictionaryRequest(
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("name")
    val name: String,
    // Null = "unchanged" (left out of the body): joining or leaving the Forum flips every dictionary
    // on the server, and a stale local copy must not undo that. Required for a new dictionary.
    @SerialName("isPublic")
    val isPublic: Boolean? = null,
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
    // ISO-8601. Sent for completeness, but the server currently owns these: Hibernate's
    // @CreationTimestamp / @UpdateTimestamp overwrite whatever the client supplies (verified —
    // the request is accepted, the sent value is simply replaced). They matter here only if the
    // backend later honours a client value, e.g. to preserve a row created offline.
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
)