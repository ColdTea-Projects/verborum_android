package de.coldtea.verborum.bibliotheca.word.data.api.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class WordRequest(
    @SerialName("wordId")
    var wordId: String?,
    @SerialName("dictionaryId")
    var dictionaryId: String?,
    @SerialName("word")
    var word: String?,
    @SerialName("wordMeta")
    var wordMeta: String?,
    @SerialName("translation")
    var translation: String?,
    @SerialName("translationMeta")
    var translationMeta: String?,
)