package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.UploadDictionaryUserCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import javax.inject.Inject

class SyncService @Inject constructor(
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
    private val uploadDictionaryUserCase: UploadDictionaryUserCase,
) {

    suspend fun syncDictionaries() = syncUserDictionariesUseCase.invoke()

    suspend fun uploadDictionary(dictionary: Dictionary) = uploadDictionaryUserCase
        .invoke(dictionary)
}