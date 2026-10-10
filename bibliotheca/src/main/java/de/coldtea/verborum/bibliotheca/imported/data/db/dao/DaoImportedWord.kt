package de.coldtea.verborum.bibliotheca.imported.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.word.data.db.entity.DictionaryWordCount
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DaoImportedWord : DaoBase<ImportedWordEntity> {

    @Query("SELECT * FROM imported_word WHERE fk_dictionary_id = :dictionaryId ORDER BY created_at ASC, word_id ASC")
    abstract fun observeByDictionary(dictionaryId: String): Flow<List<ImportedWordEntity>>

    @Query("SELECT * FROM imported_word WHERE fk_dictionary_id = :dictionaryId")
    abstract suspend fun getByDictionary(dictionaryId: String): List<ImportedWordEntity>

    /** Every downloaded word in a dictionary of exactly this language direction. */
    @Query(
        "SELECT w.* FROM imported_word w " +
            "INNER JOIN imported_dictionary d ON w.fk_dictionary_id = d.dictionary_id " +
            "WHERE UPPER(d.from_lang) = UPPER(:fromLang) AND UPPER(d.to_lang) = UPPER(:toLang)"
    )
    abstract fun observeInLanguagePair(fromLang: String, toLang: String): Flow<List<ImportedWordEntity>>

    @Query("SELECT fk_dictionary_id, COUNT(*) AS word_count FROM imported_word GROUP BY fk_dictionary_id")
    abstract fun observeCounts(): Flow<List<DictionaryWordCount>>

    /** The user's own practice progress — the only column of a downloaded word that ever changes locally. */
    @Query("UPDATE imported_word SET level = :level WHERE word_id = :wordId")
    abstract suspend fun updateLevel(wordId: String, level: Int)

    @Query("DELETE FROM imported_word WHERE fk_dictionary_id = :dictionaryId")
    abstract suspend fun deleteByDictionary(dictionaryId: String)

    // Post-logout wipe.
    @Query("DELETE FROM imported_word")
    abstract suspend fun deleteAll()

    /**
     * Replaces a dictionary's words with the owner's current ones, keeping the user's progress:
     * a word that is still there keeps its local level, a removed one goes, a new one starts at 0.
     */
    @Transaction
    open suspend fun replaceWords(dictionaryId: String, words: List<ImportedWordEntity>) {
        val levels = getByDictionary(dictionaryId).associate { it.wordId to it.level }
        deleteByDictionary(dictionaryId)
        insertAll(words.map { it.copy(level = levels[it.wordId] ?: it.level) })
    }
}
