package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class DictionaryRequest(
    @SerialName("dictionaryId")
    var dictionaryId: String?,
    @SerialName("userId")
    var userId: String?,
    @SerialName("name")
    var name: String?,
    @SerialName("isPublic")
    var isPublic: Boolean?,
    @SerialName("fromLang")
    var fromLang: String?,
    @SerialName("toLang")
    var toLang: String?,
)