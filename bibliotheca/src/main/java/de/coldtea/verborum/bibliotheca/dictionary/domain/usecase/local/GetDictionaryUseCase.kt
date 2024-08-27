package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import javax.inject.Inject

class GetDictionaryUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(dictionaryId: String): Dictionary =
        dictionaryRepository
            .getDictionary(dictionaryId)
            .convertToDictionary()
}