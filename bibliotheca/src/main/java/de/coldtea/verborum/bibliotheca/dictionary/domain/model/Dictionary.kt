package de.coldtea.verborum.bibliotheca.dictionary.domain.model

import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi

data class Dictionary(
    val dictionaryId: String,
    val userId: String,
    val name: String,
    val isPublic: Boolean,
    val fromLang: String,
    val toLang: String,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun convertToEntity() = DictionaryEntity(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun convertToUi() = DictionaryUi(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
