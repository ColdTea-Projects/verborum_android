package de.coldtea.verborum.app.word.domain

import de.coldtea.verborum.app.word.data.WordRepository
import javax.inject.Inject

class DeleteWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String) =
        wordRepository.deleteWords(listOf(wordId))
}