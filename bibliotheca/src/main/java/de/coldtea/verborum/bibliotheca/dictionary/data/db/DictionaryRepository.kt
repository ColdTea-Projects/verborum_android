package de.coldtea.verborum.bibliotheca.dictionary.data.db

import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.dao.DaoDictionary
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DictionaryRepository @Inject constructor(
    private val bibliothecaDatabase: BibliothecaDatabase,
) {
    suspend fun getAllDictionaries(): List<DictionaryEntity> =
        bibliothecaDatabase.daoDictionary.getAllDictionaries()

    fun observeAllDictionaries(): Flow<List<DictionaryEntity>> =
        bibliothecaDatabase.daoDictionary.observeAllDictionaries()

    suspend fun getAllDictionariesByUser(userId: String): List<DictionaryEntity> =
        bibliothecaDatabase.daoDictionary.getDictionariesByUser(userId)

    suspend fun getDictionary(dictionaryId: String): DictionaryEntity =
        bibliothecaDatabase.daoDictionary.getDictionary(dictionaryId)

    fun observeDictionary(dictionaryId: String): Flow<DictionaryEntity?> =
        bibliothecaDatabase.daoDictionary.observeDictionary(dictionaryId)

    fun observePendingUploadCount(): Flow<Int> =
        bibliothecaDatabase.daoDictionary.observePendingUploadCount()

    suspend fun saveDictionary(dictionaryEntity: DictionaryEntity) =
        bibliothecaDatabase.daoDictionary.insert(dictionaryEntity)

    suspend fun updateDictionary(dictionaryEntity: DictionaryEntity) =
        bibliothecaDatabase.daoDictionary.update(dictionaryEntity)

    suspend fun markDictionaryDeleted(dictionaryId: String) =
        bibliothecaDatabase.daoDictionary.markDictionaryDeleted(dictionaryId)

    suspend fun reassignOwner(oldUserId: String, newUserId: String): Int =
        bibliothecaDatabase.daoDictionary.reassignOwner(oldUserId, newUserId)

    suspend fun deleteDictionary(dictionaryId: String) =
        bibliothecaDatabase.daoDictionary.deleteDictionary(dictionaryId)

    suspend fun deleteAllDictionaries() =
        bibliothecaDatabase.daoDictionary.deleteAllDictionaries()
}