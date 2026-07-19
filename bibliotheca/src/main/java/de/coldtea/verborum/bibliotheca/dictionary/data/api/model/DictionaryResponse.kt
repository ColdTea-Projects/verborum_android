package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
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
) {
    /**
     * The dictionary DTO carries no timestamps, so the caller supplies them: the existing local
     * [createdAt]/[updatedAt] when this row is already known (preserving them across re-downloads),
     * or the current time for a row first seen now.
     */
    fun convertToDictionary(createdAt: Long, updatedAt: Long) = Dictionary(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        isSynced = true,
        fromLang = fromLang,
        toLang = toLang.orEmpty(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}