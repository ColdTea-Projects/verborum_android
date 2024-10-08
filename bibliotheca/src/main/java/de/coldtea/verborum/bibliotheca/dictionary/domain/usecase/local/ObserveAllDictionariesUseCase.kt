package de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local

import de.coldtea.verborum.bibliotheca.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAllDictionariesUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    fun invoke(): Flow<List<Dictionary>> =
        dictionaryRepository
            .observeAllDictionaries()
            .map { it.map(DictionaryEntity::convertToDictionary) }
}