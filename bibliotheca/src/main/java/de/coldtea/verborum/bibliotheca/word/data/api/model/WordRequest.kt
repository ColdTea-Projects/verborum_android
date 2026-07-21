package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class WordRequest(
    @SerialName("wordId")
    val wordId: String,
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("word")
    val word: String,
    @SerialName("wordMeta")
    val wordMeta: String,
    @SerialName("translation")
    val translation: String,
    @SerialName("translationMeta")
    val translationMeta: String,
    // Practice progress, 0..7. Now synced so it survives a reinstall and follows the user across
    // devices; the local value is already clamped, so it is sent as-is.
    @SerialName("level")
    val level: Int,
    // ISO-8601. Server-owned like the dictionary's — sent for completeness, see DictionaryRequest.
    @SerialName("createdAt")
    val createdAt: String? = null,
    @SerialName("updatedAt")
    val updatedAt: String? = null,
)