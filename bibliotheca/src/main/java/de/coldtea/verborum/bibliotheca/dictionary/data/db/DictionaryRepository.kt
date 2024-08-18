package de.coldtea.verborum.bibliotheca.dictionary.data.db

import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.dao.DaoDictionary
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import javax.inject.Inject

class DictionaryRepository @Inject constructor(
    private val bibliothecaDatabase: BibliothecaDatabase,
) {
    suspend fun getAllDictionaries(): List<DictionaryEntity> =
        bibliothecaDatabase.daoDictionary.getAllDictionaries()

    suspend fun getAllDictionariesByUser(userId: String): List<DictionaryEntity> =
        bibliothecaDatabase.daoDictionary.getDictionariesByUser(userId)

    suspend fun getDictionary(dictionaryId: String): DictionaryEntity =
        bibliothecaDatabase.daoDictionary.getDictionary(dictionaryId)

    suspend fun saveDictionary(dictionaryEntity: DictionaryEntity) =
        bibliothecaDatabase.daoDictionary.insert(dictionaryEntity)

    suspend fun updateDictionary(dictionaryEntity: DictionaryEntity) =
        bibliothecaDatabase.daoDictionary.update(dictionaryEntity)

    suspend fun deleteDictionary(dictionaryId: String) =
        bibliothecaDatabase.daoDictionary.deleteDictionary(dictionaryId)
}