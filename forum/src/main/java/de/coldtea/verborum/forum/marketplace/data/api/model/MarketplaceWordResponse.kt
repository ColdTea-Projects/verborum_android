package de.coldtea.verborum.forum.marketplace.data.api.model

import android.annotation.SuppressLint
import androidx.annotation.Keep
import de.coldtea.verborum.core.extensions.json
import de.coldtea.verborum.forum.marketplace.domain.model.MarketplaceWord
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A word of someone else's public dictionary, as ms_dictionary's `GET /words/dictionary/{id}`
 * returns it (guide §4.5). `word`/`translation` are the canonical surfaces columns — JSON arrays of
 * alternatives, e.g. `["buy","purchase"]`. `level` is deliberately absent: on another user's words
 * it is always null (guide §5.5).
 */
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class MarketplaceWordResponse(
    @SerialName("wordId")
    val wordId: String,
    @SerialName("dictionaryId")
    val dictionaryId: String,
    @SerialName("word")
    val word: String? = null,
    @SerialName("translation")
    val translation: String? = null,
) {
    fun convertToWord() = MarketplaceWord(
        wordId = wordId,
        dictionaryId = dictionaryId,
        surfaces = parseSurfaces(word),
        translations = parseSurfaces(translation),
    )

    /** Other people's data: a malformed column degrades to its raw text instead of failing. */
    @OptIn(ExperimentalSerializationApi::class)
    private fun parseSurfaces(raw: String?): List<String> {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return emptyList()
        return runCatching { json.decodeFromString<List<String>>(trimmed) }
            .getOrDefault(listOf(trimmed))
            .map(String::trim)
            .filter(String::isNotEmpty)
    }
}
