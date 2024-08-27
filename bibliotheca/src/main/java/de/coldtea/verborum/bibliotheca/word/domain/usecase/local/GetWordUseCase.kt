package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

class GetWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String): Word =
        wordRepository
            .getWord(wordId)
            .convertToWord()
}