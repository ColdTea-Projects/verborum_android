package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.bibliotheca.common.data.api.ApiTimestamp
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class DictionaryResponse(
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("userId")
    val userId: String,
    @SerialName("name")
    val name: String,
    @SerialName("isPublic")
    val isPublic: Boolean,
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
    // Server-owned, written by Hibernate's @CreationTimestamp / @UpdateTimestamp and serialised as
    // ISO-8601 without an offset ("2026-07-19T21:33:37.027968"). Nullable so a backend that does
    // not expose them yet still deserialises — see the fallbacks in convertToDictionary.
    @SerialName("creationTimestamp")
    val creationTimestamp: String? = null,
    @SerialName("updateTimestamp")
    val updateTimestamp: String? = null,
) {
    /**
     * The server's own timestamps win when present — that is what makes a creation date survive a
     * reinstall instead of every row reading "0 min ago". The fallbacks cover a backend that omits
     * them: the existing local value when this row is already known, else the current time.
     */
    fun convertToDictionary(fallbackCreatedAt: Long, fallbackUpdatedAt: Long) = Dictionary(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        isSynced = true,
        fromLang = fromLang,
        toLang = toLang.orEmpty(),
        createdAt = ApiTimestamp.parse(creationTimestamp) ?: fallbackCreatedAt,
        updatedAt = ApiTimestamp.parse(updateTimestamp) ?: fallbackUpdatedAt,
    )
}