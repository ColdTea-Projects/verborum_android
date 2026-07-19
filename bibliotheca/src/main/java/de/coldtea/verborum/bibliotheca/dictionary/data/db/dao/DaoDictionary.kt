package de.coldtea.verborum.bibliotheca.dictionary.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDictionary: DaoBase<DictionaryEntity> {

    @Transaction
    @Query("SELECT * FROM dictionary")
    suspend fun getAllDictionaries(): List<DictionaryEntity>

    // UI-facing: tombstoned rows are hidden. The suspend variant above stays raw for sync.
    //
    // The ORDER BY is load-bearing, not cosmetic: DaoBase.insert uses OnConflictStrategy.REPLACE,
    // which SQLite implements as DELETE + INSERT, so every row re-saved by a sync gets a new
    // rowid. Without an explicit sort the query returns rows in rowid order, meaning the list
    // silently reorders itself after every sync. Sorting by a stable column keeps the order fixed
    // so identical data produces an identical list — and therefore no recomposition at all.
    @Query(
        "SELECT * FROM dictionary WHERE is_deleted = 0 " +
            "ORDER BY created_at ASC, dictionary_id ASC"
    )
    fun observeAllDictionaries(): Flow<List<DictionaryEntity>>

    @Transaction
    @Query("UPDATE dictionary SET is_deleted = 1 WHERE dictionary_id = :dictionaryId")
    suspend fun markDictionaryDeleted(dictionaryId: String)

    @Transaction
    @Query("SELECT * FROM dictionary WHERE fk_user_id = :userId")
    suspend fun getDictionariesByUser(userId: String): List<DictionaryEntity>

    @Transaction
    @Query("SELECT * FROM dictionary WHERE dictionary_id = :dictionaryId")
    suspend fun getDictionary(dictionaryId: String): DictionaryEntity

    @Transaction
    @Query("SELECT * FROM dictionary WHERE dictionary_id = :dictionaryId AND is_deleted = 0")
    fun observeDictionary(dictionaryId: String): Flow<DictionaryEntity?>

    @Transaction
    @Query("DELETE FROM dictionary WHERE dictionary_id = :dictionaryId")
    suspend fun deleteDictionary(dictionaryId: String)
}