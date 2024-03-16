package de.coldtea.verborum.app.dictionary.domain

import de.coldtea.verborum.app.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.app.word.data.WordRepository
import javax.inject.Inject

class DeleteDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
) {

    suspend fun invoke(dictionaryId: String) {
        wordRepository.deleteWordsByDictionary(dictionaryId)
        dictionaryRepository.deleteDictionary(dictionaryId)
    }
}