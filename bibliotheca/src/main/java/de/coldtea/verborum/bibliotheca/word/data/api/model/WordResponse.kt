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
    /**
     * The word DTO carries no timestamps, so the caller supplies them: the existing local
     * [createdAt]/[updatedAt] when this word is already known (preserving them across re-downloads),
     * or the current time for a word first seen now.
     */
    fun convertToWord(dictionaryId: String, createdAt: Long, updatedAt: Long): Word =
        Word(
            dictionaryId = dictionaryId,
            wordId = wordId.orEmpty(),
            word = word.orEmpty(),
            wordMeta = wordMeta.orEmpty(),
            translation = translation.orEmpty(),
            translationMeta = translationMeta.orEmpty(),
            isSynced = true,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}