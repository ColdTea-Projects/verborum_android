package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveWordsByDictionaryUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    fun invoke(dictionaryId: String): Flow<List<Word>> =
        wordRepository
            .observeWordsByDictionary(dictionaryId)
            .map { it.map(WordEntity::convertToWord) }
}