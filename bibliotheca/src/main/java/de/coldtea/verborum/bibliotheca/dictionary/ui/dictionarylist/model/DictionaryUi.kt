package de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary

data class DictionaryUi(
    val dictionaryId: String,
    val userId: String,
    val name: String,
    val isPublic: Boolean,
    val fromLang: String,
    val toLang: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: List<String> = emptyList(),
    // UI-only: live word count, populated by combining with word data (0 when unknown).
    val wordCount: Int = 0,
    // Downloaded from the Forum: someone else's, read-only, never converted back or uploaded.
    val isImported: Boolean = false,
    // The publisher's display name, for a downloaded dictionary whose name is known.
    val publisherName: String? = null,
) {
    fun convertToDictionary() = Dictionary(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        isSynced = false,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tags,
    )
}
