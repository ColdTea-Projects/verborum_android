package de.coldtea.verborum.app.word.domain

import de.coldtea.verborum.app.word.data.WordRepository
import de.coldtea.verborum.app.word.domain.model.Word
import javax.inject.Inject

class GetWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String): Word =
        wordRepository
            .getWord(wordId)
            .convertToWord()
}