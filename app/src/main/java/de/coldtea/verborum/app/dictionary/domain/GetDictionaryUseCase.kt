package de.coldtea.verborum.app.dictionary.domain

import de.coldtea.verborum.app.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.app.dictionary.domain.model.Dictionary
import javax.inject.Inject

class GetDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(dictionaryId: String): Dictionary =
        dictionaryRepository
            .getDictionary(dictionaryId)
            .convertToDictionary()
}