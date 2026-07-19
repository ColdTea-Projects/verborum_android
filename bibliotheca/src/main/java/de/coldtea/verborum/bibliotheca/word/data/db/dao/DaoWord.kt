package de.coldtea.verborum.bibliotheca.word.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.word.data.db.entity.DictionaryWordCount
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoWord: DaoBase<WordEntity> {

    @Transaction
    @Query("SELECT * FROM word WHERE fk_dictionary_id = :dictionaryId")
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity>

    // UI-facing: tombstoned rows are hidden. The suspend variant above stays raw for sync.
    @Transaction
    @Query("SELECT * FROM word WHERE fk_dictionary_id = :dictionaryId AND is_deleted = 0")
    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordEntity>>

    /**
     * Every live word that belongs to a dictionary with the *same language pair* as
     * [dictionaryId] — including that dictionary's own words.
     *
     * Feeds the multiple-choice distractor pool: wrong answers are drawn from the whole language
     * pair rather than a single dictionary, so a small dictionary still gets plausible choices.
     * Tombstoned words and tombstoned dictionaries are excluded on both sides of the join.
     */
    @Transaction
    @Query(
        "SELECT w.* FROM word w " +
            "INNER JOIN dictionary d ON w.fk_dictionary_id = d.dictionary_id " +
            "WHERE w.is_deleted = 0 AND d.is_deleted = 0 " +
            "AND d.from_lang = (SELECT from_lang FROM dictionary WHERE dictionary_id = :dictionaryId) " +
            "AND d.to_lang = (SELECT to_lang FROM dictionary WHERE dictionary_id = :dictionaryId)"
    )
    fun observeWordsInLanguagePairOf(dictionaryId: String): Flow<List<WordEntity>>

    // Live word count per dictionary; tombstoned rows are excluded.
    @Query(
        "SELECT fk_dictionary_id, COUNT(*) AS word_count FROM word " +
            "WHERE is_deleted = 0 GROUP BY fk_dictionary_id"
    )
    fun observeWordCounts(): Flow<List<DictionaryWordCount>>

    @Transaction
    @Query("UPDATE word SET is_deleted = 1 WHERE word_id = :wordId")
    suspend fun markWordDeleted(wordId: String)

    @Transaction
    @Query("SELECT * FROM word WHERE word_id = :wordId")
    suspend fun getWord(wordId: String): WordEntity

    @Transaction
    @Query("DELETE FROM word WHERE word_id IN (:wordIds)")
    suspend fun deleteWords(wordIds: List<String>)

    @Transaction
    @Query("DELETE FROM word WHERE fk_dictionary_id = :dictionaryId")
    suspend fun deleteWordsByDictionary(dictionaryId: String)
}