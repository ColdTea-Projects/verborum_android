package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

class GetWordsByDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(dictionaryId: String): List<Word> =
        wordRepository
            .getWordsByDictionary(dictionaryId)
            .map(WordEntity::convertToWord)
}