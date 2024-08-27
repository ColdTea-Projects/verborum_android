package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import javax.inject.Inject

class DeleteWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String) =
        wordRepository.deleteWords(listOf(wordId))
}