package de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local

import javax.inject.Inject

class CleanDictionariesUseCase @Inject constructor(
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
) {

    suspend fun invoke() {
        val dictionaries = getAllDictionariesUseCase.invoke()
        dictionaries.map {
            deleteDictionaryUseCase.invoke(it.dictionaryId)
        }
    }
}