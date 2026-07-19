package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.bibliotheca.common.data.api.ApiTimestamp
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
    // Server-owned, written by Hibernate's @CreationTimestamp / @UpdateTimestamp and serialised as
    // ISO-8601 without an offset ("2026-07-19T21:34:11.858866"). Nullable so a backend that does
    // not expose them yet still deserialises — see the fallbacks in convertToWord.
    @SerialName("creationTimestamp")
    var creationTimestamp: String? = null,
    @SerialName("updateTimestamp")
    var updateTimestamp: String? = null,
) {
    /**
     * The server's own timestamps win when it sends them; the fallbacks cover a backend that
     * still omits them (existing local value, else the current time).
     *
     * [level] is device-local practice progress the backend does not store at all — the caller
     * passes the local value through so a sync cannot wipe it.
     */
    fun convertToWord(
        dictionaryId: String,
        fallbackCreatedAt: Long,
        fallbackUpdatedAt: Long,
        level: Int = 0,
    ): Word =
        Word(
            dictionaryId = dictionaryId,
            wordId = wordId.orEmpty(),
            word = word.orEmpty(),
            wordMeta = wordMeta.orEmpty(),
            translation = translation.orEmpty(),
            translationMeta = translationMeta.orEmpty(),
            isSynced = true,
            createdAt = ApiTimestamp.parse(creationTimestamp) ?: fallbackCreatedAt,
            updatedAt = ApiTimestamp.parse(updateTimestamp) ?: fallbackUpdatedAt,
            level = level,
        )
}