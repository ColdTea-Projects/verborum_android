package de.coldtea.verborum.bibliotheca.word.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.bibliotheca.common.data.db.DaoBase
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoWord: DaoBase<WordEntity> {

    @Transaction
    @Query("SELECT * FROM word WHERE fk_dictionary_id = :dictionaryId")
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity>

    @Transaction
    @Query("SELECT * FROM word WHERE fk_dictionary_id = :dictionaryId")
    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordEntity>>

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