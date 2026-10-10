package de.coldtea.verborum.bibliotheca.imported.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoImportedDictionary : DaoBase<ImportedDictionaryEntity> {

    // Stable order (see DaoDictionary.observeAllDictionaries): REPLACE re-inserts rows on refresh.
    @Query("SELECT * FROM imported_dictionary ORDER BY imported_at DESC, dictionary_id ASC")
    fun observeAll(): Flow<List<ImportedDictionaryEntity>>

    @Query("SELECT * FROM imported_dictionary")
    suspend fun getAll(): List<ImportedDictionaryEntity>

    @Query("SELECT * FROM imported_dictionary WHERE dictionary_id = :dictionaryId")
    fun observe(dictionaryId: String): Flow<ImportedDictionaryEntity?>

    @Query("SELECT dictionary_id FROM imported_dictionary")
    fun observeIds(): Flow<List<String>>

    @Query("DELETE FROM imported_dictionary WHERE dictionary_id = :dictionaryId")
    suspend fun delete(dictionaryId: String)

    // Post-logout wipe.
    @Query("DELETE FROM imported_dictionary")
    suspend fun deleteAll()
}
