package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.api.CreateDictionaryUserCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.api.DeleteDictionaryUserCase
import javax.inject.Inject

class UploadService @Inject constructor(
    private val createDictionaryUserCase: CreateDictionaryUserCase,
    private val deleteDictionaryUserCase: DeleteDictionaryUserCase,
) {

    suspend fun createDictionary(dictionary: Dictionary) = createDictionaryUserCase
        .invoke(dictionary)

    suspend fun deleteDictionary(dictionaryId: String) = deleteDictionaryUserCase
        .invoke(dictionaryId)
}