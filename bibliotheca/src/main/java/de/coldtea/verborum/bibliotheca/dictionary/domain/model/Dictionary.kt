package de.coldtea.verborum.bibliotheca.dictionary.domain.model

import de.coldtea.verborum.bibliotheca.common.data.api.ApiTimestamp
import de.coldtea.verborum.bibliotheca.dictionary.data.api.model.DictionaryRequest
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi

data class Dictionary(
    val dictionaryId: String,
    val userId: String,
    val name: String,
    val isPublic: Boolean,
    val isSynced: Boolean,
    val isDeleted: Boolean = false,
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
        isSynced = isSynced,
        isDeleted = isDeleted,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun convertToRequest() = DictionaryRequest(
        dictionaryId = dictionaryId,
        userId = userId,
        name = name,
        isPublic = isPublic,
        fromLang = fromLang,
        toLang = toLang,
        createdAt = ApiTimestamp.format(createdAt),
        updatedAt = ApiTimestamp.format(updatedAt),
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
