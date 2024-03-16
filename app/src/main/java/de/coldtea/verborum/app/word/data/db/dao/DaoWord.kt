package de.coldtea.verborum.app.word.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import de.coldtea.verborum.app.common.data.db.DaoBase
import de.coldtea.verborum.app.word.data.db.entity.WordEntity

@Dao
interface DaoWord: DaoBase<WordEntity> {

    @Transaction
    @Query("SELECT * FROM word WHERE fk_dictionary_id = :dictionaryId")
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity>

    @Transaction
    @Query("SELECT * FROM word WHERE word_id = :wordId")
    suspend fun getWord(wordId: String): WordEntity
}