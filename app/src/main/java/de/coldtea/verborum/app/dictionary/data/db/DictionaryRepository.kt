package de.coldtea.verborum.app.dictionary.data.db

import de.coldtea.verborum.app.dictionary.data.db.dao.DaoDictionary
import de.coldtea.verborum.app.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.app.word.data.db.entity.WordEntity
import javax.inject.Inject

class DictionaryRepository @Inject constructor(
    private val daoDictionary: DaoDictionary,
) {
    suspend fun getAllDictionaries(): List<DictionaryEntity> =
        daoDictionary.getAllDictionaries()

    suspend fun getAllDictionariesByUser(userId: String): List<DictionaryEntity> =
        daoDictionary.getDictionariesByUser(userId)

    suspend fun getDictionary(dictionaryId: String): DictionaryEntity =
        daoDictionary.getDictionary(dictionaryId)

    suspend fun saveDictionary(dictionaryEntity: DictionaryEntity) =
        daoDictionary.insert(dictionaryEntity)

    suspend fun updateDictionary(dictionaryEntity: DictionaryEntity) =
        daoDictionary.update(dictionaryEntity)

    suspend fun deleteDictionary(dictionaryId: String) =
        daoDictionary.deleteDictionary(dictionaryId)
}