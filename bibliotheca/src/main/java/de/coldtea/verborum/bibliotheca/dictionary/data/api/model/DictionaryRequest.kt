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
    @SerialName("isPublic")
    val isPublic: Boolean,
    @SerialName("fromLang")
    val fromLang: String,
    @SerialName("toLang")
    val toLang: String,
)