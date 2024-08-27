package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import javax.inject.Inject

class DeleteWordByDictionaryIdUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(dictionaryId: String) =
        wordRepository.deleteWordsByDictionary(dictionaryId)
}