package de.coldtea.verborum.bibliotheca.word.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.bibliotheca.common.data.api.ApiTimestamp
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

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
    // Captured as a raw JSON element, not an Int, so a malformed value (a string, a float, null,
    // or a missing field) can never fail the whole word's deserialisation — it is validated in
    // convertToWord instead. See [validLevel].
    @SerialName("level")
    var level: JsonElement? = null,
    // Server-owned, written by Hibernate's @CreationTimestamp / @UpdateTimestamp and serialised as
    // ISO-8601 in UTC ("2026-07-21T15:57:10.124480Z"). Nullable so a backend that does not expose
    // them still deserialises — see the fallbacks in convertToWord.
    @SerialName("createdAt")
    var createdAt: String? = null,
    @SerialName("updatedAt")
    var updatedAt: String? = null,
) {
    /**
     * The practice [level] the server sent, but only when it is a whole number in 0..7. Anything
     * else — null, missing, out of range, or not a number at all — is treated as invalid.
     */
    private val validLevel: Int?
        get() = (level as? JsonPrimitive)?.intOrNull?.takeIf { it in 0..7 }

    /**
     * Server timestamps win when present; the fallbacks cover a backend that still omits them.
     *
     * The practice level is taken from the server, but a value that is not a valid 0..7 is reset
     * to 0 **and** the row is marked unsynced — so the corrected value is uploaded back and the
     * bad data is healed on the server too.
     */
    fun convertToWord(
        dictionaryId: String,
        fallbackCreatedAt: Long,
        fallbackUpdatedAt: Long,
    ): Word =
        Word(
            dictionaryId = dictionaryId,
            wordId = wordId.orEmpty(),
            word = word.orEmpty(),
            wordMeta = wordMeta.orEmpty(),
            translation = translation.orEmpty(),
            translationMeta = translationMeta.orEmpty(),
            // A valid level means the row matches the server; an invalid one must be re-pushed.
            isSynced = validLevel != null,
            createdAt = ApiTimestamp.parse(createdAt) ?: fallbackCreatedAt,
            updatedAt = ApiTimestamp.parse(updatedAt) ?: fallbackUpdatedAt,
            level = validLevel ?: 0,
        )
}