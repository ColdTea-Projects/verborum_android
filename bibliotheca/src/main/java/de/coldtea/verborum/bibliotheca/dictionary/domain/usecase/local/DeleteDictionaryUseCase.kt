package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
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