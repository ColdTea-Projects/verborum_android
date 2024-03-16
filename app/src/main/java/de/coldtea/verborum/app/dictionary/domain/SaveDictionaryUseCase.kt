package de.coldtea.verborum.app.dictionary.domain

import de.coldtea.verborum.app.common.utils.generateUUIDV4
import de.coldtea.verborum.app.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.app.dictionary.domain.model.Dictionary
import javax.inject.Inject

class SaveDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(dictionary: Dictionary): String =
        if (dictionary.dictionaryId.isBlank()) {
            createNewDictionary(dictionary)
        } else {
            updateDictionary(dictionary)
        }

    suspend fun createNewDictionary(dictionary: Dictionary): String {
        val dictionaryId = generateUUIDV4()
        dictionaryRepository.saveDictionary(
            dictionary.copy(dictionaryId = dictionaryId).convertToEntity()
        )

        return dictionaryId
    }

    suspend fun updateDictionary(dictionary: Dictionary): String {
        dictionaryRepository.saveDictionary(
            dictionary.convertToEntity()
        )

        return dictionary.dictionaryId
    }
}