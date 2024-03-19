package de.coldtea.verborum.bibliotheca.dictionary.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity

@Dao
interface DaoDictionary: DaoBase<DictionaryEntity> {

    @Transaction
    @Query("SELECT * FROM dictionary")
    suspend fun getAllDictionaries(): List<DictionaryEntity>

    @Transaction
    @Query("SELECT * FROM dictionary WHERE fk_user_id = :userId")
    suspend fun getDictionariesByUser(userId: String): List<DictionaryEntity>

    @Transaction
    @Query("SELECT * FROM dictionary WHERE dictionary_id = :dictionaryId")
    suspend fun getDictionary(dictionaryId: String): DictionaryEntity

    @Transaction
    @Query("DELETE FROM dictionary WHERE dictionary_id = :dictionaryId")
    suspend fun deleteDictionary(dictionaryId: String)
}