package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Every word across all dictionaries sharing the language pair of [dictionaryId]. Used to source
 * multiple-choice distractors and to decide whether a test can be built at all.
 */
class ObserveWordsInLanguagePairUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    fun invoke(dictionaryId: String): Flow<List<Word>> =
        wordRepository
            .observeWordsInLanguagePairOf(dictionaryId)
            .map { it.map(WordEntity::convertToWord) }
}
