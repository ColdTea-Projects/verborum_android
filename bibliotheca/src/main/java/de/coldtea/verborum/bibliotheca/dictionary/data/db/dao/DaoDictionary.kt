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

    // UI-facing: tombstoned rows are hidden, newest first.
    //
    // The ORDER BY is load-bearing, not cosmetic: DaoBase.insert uses OnConflictStrategy.REPLACE,
    // which SQLite implements as DELETE + INSERT, so every row re-saved by a sync gets a new
    // rowid. Without an explicit sort the query returns rows in rowid order, meaning the list
    // silently reorders itself after every sync. Sorting by a stable column keeps the order fixed
    // so identical data produces an identical list — and therefore no recomposition at all.
    @Query(
        "SELECT * FROM dictionary WHERE is_deleted = 0 " +
            "ORDER BY created_at DESC, dictionary_id ASC"
    )
    fun observeAllDictionaries(): Flow<List<DictionaryEntity>>

    // Rows the server has not seen yet: never-synced/edited (isSynced = 0) or pending remote
    // deletion (is_deleted = 1). Drives the immediate upload trigger.
    @Query("SELECT COUNT(*) FROM dictionary WHERE isSynced = 0 OR is_deleted = 1")
    fun observePendingUploadCount(): Flow<Int>

    @Transaction
    @Query("UPDATE dictionary SET is_deleted = 1 WHERE dictionary_id = :dictionaryId")
    suspend fun markDictionaryDeleted(dictionaryId: String)

    /**
     * Flips the synced flag after a successful upload, touching no other column.
     *
     * The upload snapshots the row, does network I/O, then comes back to record success — writing
     * the whole snapshot back (DaoBase.insert is REPLACE) would revert any edit the user made in
     * between. [updatedAt] pins the version that was actually uploaded: if the row changed while
     * the request was in flight the update matches nothing, the row stays unsynced, and the next
     * sync uploads the newer content.
     */
    @Transaction
    @Query(
        "UPDATE dictionary SET isSynced = 1 " +
            "WHERE dictionary_id = :dictionaryId AND updated_at = :updatedAt AND is_deleted = 0"
    )
    suspend fun markDictionarySynced(dictionaryId: String, updatedAt: Long)

    // Guest-data migration on first login (guide §9.7): re-own the guest's dictionaries under the
    // signed-in subject and re-flag them unsynced so the normal upload pushes them to the server.
    // The guest UUID must never reach the backend, so this runs before the first authenticated sync.
    @Transaction
    @Query(
        "UPDATE dictionary SET fk_user_id = :newUserId, isSynced = 0 " +
            "WHERE fk_user_id = :oldUserId"
    )
    suspend fun reassignOwner(oldUserId: String, newUserId: String): Int

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

    // Post-logout wipe: owner-keyed data must never survive into the next account's session.
    @Transaction
    @Query("DELETE FROM dictionary")
    suspend fun deleteAllDictionaries()
}