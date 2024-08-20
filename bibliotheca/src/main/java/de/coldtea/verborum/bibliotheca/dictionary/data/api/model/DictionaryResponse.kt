package de.coldtea.verborum.bibliotheca.dictionary.data.api.model

import androidx.annotation.Keep
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class DictionaryResponse(
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
) {
    fun convertToDictionary() = Dictionary(
        dictionaryId = dictionaryId.orEmpty(),
        userId = userId.orEmpty(),
        name = name.orEmpty(),
        isPublic = isPublic?:false,
        fromLang = fromLang.orEmpty(),
        toLang = toLang.orEmpty(),
        createdAt = 1724102088L,
        updatedAt = 1724102088L,
    )
}