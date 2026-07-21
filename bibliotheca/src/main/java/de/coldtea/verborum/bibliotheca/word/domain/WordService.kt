package de.coldtea.verborum.bibliotheca.word.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordByDictionaryIdApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordByDictionaryIdUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.MarkWordDeletedUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordCountsUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsInLanguagePairUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordService @Inject constructor(
    private val observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase,
    private val observeWordsInLanguagePairUseCase: ObserveWordsInLanguagePairUseCase,
    private val observeWordCountsUseCase: ObserveWordCountsUseCase,
    private val deleteWordByDictionaryIdApiUseCase: DeleteWordByDictionaryIdApiUseCase,
    private val deleteWordByDictionaryIdUseCase: DeleteWordByDictionaryIdUseCase,
    private val deleteWordApiUseCase: DeleteWordApiUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
    private val markWordDeletedUseCase: MarkWordDeletedUseCase,
    private val getWordUseCase: GetWordUseCase,
    private val saveWordUseCase: SaveWordUseCase,
) {

    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordUi>> =
        observeWordsByDictionaryUseCase
            .invoke(dictionaryId)
            .map { it.map(Word::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    /**
     * Words from every dictionary sharing this one's language pair (its own included). Backs the
     * test's distractor pool and the "enough words to build a test" check.
     */
    fun observeWordsInLanguagePair(dictionaryId: String): Flow<List<WordUi>> =
        observeWordsInLanguagePairUseCase
            .invoke(dictionaryId)
            .map { it.map(Word::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    /** Word count per dictionary id, for the dictionary list. */
    fun observeWordCounts(): Flow<Map<String, Int>> =
        observeWordCountsUseCase
            .invoke()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    suspend fun getWord(wordId: String): WordUi =
        getWordUseCase.invoke(wordId).convertToUi()

    suspend fun saveWord(word: Word){
        saveWordUseCase.invoke(word)
    }

    /**
     * Tombstones the word first so the deletion is immediate and offline-safe, then hard-deletes
     * once the server confirms. A failed or offline API call leaves the tombstone in place;
     * the sync upload phase retries it.
     */
    suspend fun deleteWord(wordId: String) {
        markWordDeletedUseCase.invoke(wordId)
        if (deleteWordApiUseCase.invoke(wordId).isSuccessful) {
            deleteWordUseCase.invoke(wordId)
        }
    }

    suspend fun cleanWordsInDictionary(dictionaryId: String) {
        if (deleteWordByDictionaryIdApiUseCase.invoke(dictionaryId).isSuccessful) {
            deleteWordByDictionaryIdUseCase.invoke(dictionaryId)
        }
    }

}
