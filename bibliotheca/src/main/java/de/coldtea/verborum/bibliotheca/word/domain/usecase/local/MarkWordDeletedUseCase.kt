package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import javax.inject.Inject

/**
 * Tombstones a word: hides it locally right away (works offline) so the server delete can happen
 * later. Hard deletion follows once the API confirms — see [DeleteWordUseCase].
 */
class MarkWordDeletedUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(wordId: String) =
        wordRepository.markWordDeleted(wordId)
}
