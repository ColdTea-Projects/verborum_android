package de.coldtea.verborum.app.word.data

import de.coldtea.verborum.app.common.data.db.insertOrUpdate
import de.coldtea.verborum.app.word.data.db.dao.DaoWord
import de.coldtea.verborum.app.word.data.db.entity.WordEntity
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val daoWord: DaoWord,
) {
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity> =
        daoWord.getWordsByDictionary(dictionaryId)

    suspend fun getWord(wordId: String): WordEntity =
        daoWord.getWord(wordId)

    suspend fun saveWord(wordEntity: WordEntity) =
        daoWord.insert(wordEntity)

    suspend fun updateWord(wordEntity: WordEntity) =
        daoWord.update(wordEntity)

    suspend fun deleteWords(wordIds: List<String>) =
        daoWord.deleteWords(wordIds)
}