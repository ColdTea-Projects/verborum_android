package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import javax.inject.Inject

class UpdateDictionaryApiUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
) {

    suspend fun invoke(dictionary: Dictionary) = dictionaryApi.createDictionary(
        dictionary.convertToRequest()
    )
}