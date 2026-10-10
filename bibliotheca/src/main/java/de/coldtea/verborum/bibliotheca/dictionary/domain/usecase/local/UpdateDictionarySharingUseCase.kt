package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import javax.inject.Inject

/** Writes the server's sharing state onto the local row, touching nothing else. */
class UpdateDictionarySharingUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {
    suspend fun invoke(dictionaryId: String, isPublic: Boolean) =
        dictionaryRepository.updateIsPublic(dictionaryId, isPublic)
}
