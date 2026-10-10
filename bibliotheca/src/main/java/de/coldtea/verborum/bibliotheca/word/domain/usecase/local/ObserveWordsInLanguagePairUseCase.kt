package de.coldtea.verborum.bibliotheca.word.domain.usecase.local

import de.coldtea.verborum.bibliotheca.word.data.WordRepository
import de.coldtea.verborum.bibliotheca.word.data.db.entity.WordEntity
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Every word across the user's own dictionaries of this language direction. Used to source
 * multiple-choice distractors and to decide whether a test can be built at all.
 */
class ObserveWordsInLanguagePairUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    fun invoke(fromLang: String, toLang: String): Flow<List<Word>> =
        wordRepository
            .observeWordsInLanguagePair(fromLang, toLang)
            .map { it.map(WordEntity::convertToWord) }
}
