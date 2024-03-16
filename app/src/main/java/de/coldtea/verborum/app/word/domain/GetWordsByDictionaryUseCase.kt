package de.coldtea.verborum.app.word.domain

import de.coldtea.verborum.app.word.data.WordRepository
import de.coldtea.verborum.app.word.domain.model.Word
import javax.inject.Inject

class GetWordsByDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(dictionaryId: String): List<Word> =
        wordRepository
            .getWordsByDictionary(dictionaryId)
            .map { it.convertToWord() }
}