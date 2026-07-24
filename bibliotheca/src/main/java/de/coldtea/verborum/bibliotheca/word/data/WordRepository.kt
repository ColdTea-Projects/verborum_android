package de.coldtea.verborum.bibliotheca.word.data

import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.common.data.db.insertOrUpdate
import de.coldtea.verborum.bibliotheca.word.data.db.dao.DaoWord
import de.coldtea.verborum.bibliotheca.word.data.db.entity.DictionaryWordCount
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WordRepository @Inject constructor(
    private val bibliothecaDatabase: BibliothecaDatabase,
) {
    suspend fun getWordsByDictionary(dictionaryId: String): List<WordEntity> =
        bibliothecaDatabase.daoWord.getWordsByDictionary(dictionaryId)

    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordEntity>> =
        bibliothecaDatabase.daoWord.observeWordsByDictionary(dictionaryId)

    fun observeWordsInLanguagePairOf(dictionaryId: String): Flow<List<WordEntity>> =
        bibliothecaDatabase.daoWord.observeWordsInLanguagePairOf(dictionaryId)

    fun observeWordCounts(): Flow<List<DictionaryWordCount>> =
        bibliothecaDatabase.daoWord.observeWordCounts()

    fun observePendingUploadCount(): Flow<Int> =
        bibliothecaDatabase.daoWord.observePendingUploadCount()

    suspend fun getWord(wordId: String): WordEntity =
        bibliothecaDatabase.daoWord.getWord(wordId)

    suspend fun saveWord(wordEntity: WordEntity) =
        bibliothecaDatabase.daoWord.insert(wordEntity)

    suspend fun saveWords(wordEntities: List<WordEntity>) =
        bibliothecaDatabase.daoWord.insertAll(wordEntities)

    suspend fun updateWord(wordEntity: WordEntity) =
        bibliothecaDatabase.daoWord.update(wordEntity)

    suspend fun markWordDeleted(wordId: String) =
        bibliothecaDatabase.daoWord.markWordDeleted(wordId)

    suspend fun markWordsUnsyncedForUser(userId: String) =
        bibliothecaDatabase.daoWord.markWordsUnsyncedForUser(userId)

    suspend fun deleteWords(wordIds: List<String>) =
        bibliothecaDatabase.daoWord.deleteWords(wordIds)

    suspend fun deleteWordsByDictionary(dictionaryId: String) =
        bibliothecaDatabase.daoWord.deleteWordsByDictionary(dictionaryId)

}