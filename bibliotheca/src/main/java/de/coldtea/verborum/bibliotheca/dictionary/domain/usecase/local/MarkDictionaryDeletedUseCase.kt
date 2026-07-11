package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import javax.inject.Inject

/**
 * Tombstones a dictionary: hides it locally right away (works offline) so the server delete can
 * happen later. Hard deletion follows once the API confirms — see [DeleteDictionaryUseCase].
 */
class MarkDictionaryDeletedUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend fun invoke(dictionaryId: String) =
        dictionaryRepository.markDictionaryDeleted(dictionaryId)
}
