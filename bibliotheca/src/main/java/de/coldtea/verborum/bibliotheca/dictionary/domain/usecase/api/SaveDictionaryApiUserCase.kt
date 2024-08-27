package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import javax.inject.Inject

class SaveDictionaryApiUserCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
) {

    suspend fun invoke(dictionary: Dictionary) =
        if (dictionary.dictionaryId.isBlank()) {
            val dictionaryId = generateUUIDV4()
            dictionaryApi.createDictionary(
                dictionary.copy(dictionaryId = dictionaryId).convertToRequest()
            )
        } else {
            dictionaryApi.updateDictionary(dictionary.convertToRequest())
        }
}