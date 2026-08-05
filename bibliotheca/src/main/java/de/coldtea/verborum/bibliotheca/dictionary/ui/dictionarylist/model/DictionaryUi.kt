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
