package de.coldtea.verborum.bibliotheca.word.data.db

import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val bibliothecaDatabase: BibliothecaDatabase,
) {
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity> =
        bibliothecaDatabase.daoWord.getWordsByDictionary(dictionaryId)

    suspend fun getWord(wordId: String): WordEntity =
        bibliothecaDatabase.daoWord.getWord(wordId)

    suspend fun saveWord(wordEntity: WordEntity) =
        bibliothecaDatabase.daoWord.insert(wordEntity)

    suspend fun updateWord(wordEntity: WordEntity) =
        bibliothecaDatabase.daoWord.update(wordEntity)

    suspend fun deleteWords(wordIds: List<String>) =
        bibliothecaDatabase.daoWord.deleteWords(wordIds)

    suspend fun deleteWordsByDictionary(dictionaryId: String) =
        bibliothecaDatabase.daoWord.deleteWordsByDictionary(dictionaryId)

}