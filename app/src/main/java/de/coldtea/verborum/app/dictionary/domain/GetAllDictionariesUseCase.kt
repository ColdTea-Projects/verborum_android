package de.coldtea.verborum.app.dictionary.domain

import de.coldtea.verborum.app.dictionary.data.db.DictionaryRepository
import de.coldtea.verborum.app.dictionary.data.db.entity.DictionaryEntity
import de.coldtea.verborum.app.dictionary.domain.model.Dictionary
import javax.inject.Inject

class GetAllDictionariesUseCase @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
) {

    suspend fun invoke(): List<Dictionary> =
        dictionaryRepository
            .getAllDictionaries()
            .map(DictionaryEntity::convertToDictionary)
}