package de.coldtea.verborum.bibliotheca.word.data.api.model

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class WordBundleRequest(
    @SerialName("dictionaryId")
    var dictionaryId: String?,
    @SerialName("words")
    var words: List<WordRequest>?,
)