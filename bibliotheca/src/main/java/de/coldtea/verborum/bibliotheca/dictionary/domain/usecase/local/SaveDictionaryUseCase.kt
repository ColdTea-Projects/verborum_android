package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import java.util.UUID
import javax.inject.Inject

class SaveDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(dictionary: Dictionary): String {
        val dictionaryId = dictionary.dictionaryId.ifEmpty { generateUUIDV4() }
        val dictionaryWithId = dictionary.copy(dictionaryId = dictionaryId).convertToEntity()
        dictionaryRepository.saveDictionary(dictionaryWithId)

        return dictionaryId
    }
}