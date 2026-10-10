package de.coldtea.verborum.bibliotheca.imported.data

import androidx.room.withTransaction
import de.coldtea.verborum.bibliotheca.common.data.db.BibliothecaDatabase
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedDictionaryEntity
import de.coldtea.verborum.bibliotheca.imported.data.db.entity.ImportedWordEntity
import de.coldtea.verborum.bibliotheca.word.data.db.entity.DictionaryWordCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** The local copies of downloaded dictionaries and their words. */
class ImportedDictionaryRepository @Inject constructor(
    private val bibliothecaDatabase: BibliothecaDatabase,
) {
    fun observeDictionaries(): Flow<List<ImportedDictionaryEntity>> =
        bibliothecaDatabase.daoImportedDictionary.observeAll()

    suspend fun getDictionaries(): List<ImportedDictionaryEntity> =
        bibliothecaDatabase.daoImportedDictionary.getAll()

    fun observeDictionary(dictionaryId: String): Flow<ImportedDictionaryEntity?> =
        bibliothecaDatabase.daoImportedDictionary.observe(dictionaryId)

    fun observeDictionaryIds(): Flow<List<String>> =
        bibliothecaDatabase.daoImportedDictionary.observeIds()

    fun observeWords(dictionaryId: String): Flow<List<ImportedWordEntity>> =
        bibliothecaDatabase.daoImportedWord.observeByDictionary(dictionaryId)

    fun observeWordsInLanguagePair(fromLang: String, toLang: String): Flow<List<ImportedWordEntity>> =
        bibliothecaDatabase.daoImportedWord.observeInLanguagePair(fromLang, toLang)

    fun observeWordCounts(): Flow<List<DictionaryWordCount>> =
        bibliothecaDatabase.daoImportedWord.observeCounts()

    suspend fun updateWordLevel(wordId: String, level: Int) =
        bibliothecaDatabase.daoImportedWord.updateLevel(wordId, level)

    /** The dictionary and its words in one transaction, so a reader never sees half of it. */
    suspend fun saveDictionary(dictionary: ImportedDictionaryEntity, words: List<ImportedWordEntity>) =
        bibliothecaDatabase.withTransaction {
            bibliothecaDatabase.daoImportedDictionary.insert(dictionary)
            bibliothecaDatabase.daoImportedWord.replaceWords(dictionary.dictionaryId, words)
        }

    suspend fun deleteDictionary(dictionaryId: String) =
        bibliothecaDatabase.withTransaction {
            bibliothecaDatabase.daoImportedWord.deleteByDictionary(dictionaryId)
            bibliothecaDatabase.daoImportedDictionary.delete(dictionaryId)
        }

    suspend fun deleteAll() =
        bibliothecaDatabase.withTransaction {
            bibliothecaDatabase.daoImportedWord.deleteAll()
            bibliothecaDatabase.daoImportedDictionary.deleteAll()
        }
}
