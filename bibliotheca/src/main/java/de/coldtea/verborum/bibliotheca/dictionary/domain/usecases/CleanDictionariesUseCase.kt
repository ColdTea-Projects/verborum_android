package de.coldtea.verborum.bibliotheca.dictionary.domain.usecases

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.word.data.WordRepository
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