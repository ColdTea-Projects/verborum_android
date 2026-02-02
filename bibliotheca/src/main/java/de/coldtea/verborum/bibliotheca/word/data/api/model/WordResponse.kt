package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class WordResponse(
    @SerialName("wordId")
    var wordId: String?,
    @SerialName("word")
    var word: String?,
    @SerialName("wordMeta")
    var wordMeta: String?,
    @SerialName("translation")
    var translation: String?,
    @SerialName("translationMeta")
    var translationMeta: String?,
) {
    fun convertToWord(dictionaryId: String): Word =
        Word(
            dictionaryId = dictionaryId,
            wordId = wordId.orEmpty(),
            word = word.orEmpty(),
            wordMeta = wordMeta.orEmpty(),
            translation = translation.orEmpty(),
            translationMeta = translationMeta.orEmpty(),
            isSynced = true,
            createdAt = 1724102088L,
            updatedAt = 1724102088L
        )
}