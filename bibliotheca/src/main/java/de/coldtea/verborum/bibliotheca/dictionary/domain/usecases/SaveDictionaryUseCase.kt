package de.coldtea.verborum.bibliotheca.dictionary.domain.usecases

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import javax.inject.Inject

class SaveDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(dictionary: Dictionary): String {
        dictionaryRepository.saveDictionary(
            dictionary.convertToEntity()
        )

        return dictionary.dictionaryId
    }
}