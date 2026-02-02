package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import javax.inject.Inject

class DeleteDictionaryApiUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
) {

    suspend fun invoke(dictionaryId: String) =
        dictionaryApi.deleteDictionary(dictionaryId)
}