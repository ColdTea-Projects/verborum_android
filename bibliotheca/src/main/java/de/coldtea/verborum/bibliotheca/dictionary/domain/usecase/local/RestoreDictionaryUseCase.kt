package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import javax.inject.Inject

/** Lifts a deletion tombstone — for a delete the server refused, so the dictionary shows again. */
class RestoreDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend fun invoke(dictionaryId: String) = dictionaryRepository.restoreDictionary(dictionaryId)
}
