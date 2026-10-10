package de.coldtea.verborum.bibliotheca.word.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.ImportedDictionaryService
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.DeleteWordApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.DeleteWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.GetWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.MarkWordDeletedUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordCountsUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsByDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.ObserveWordsInLanguagePairUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.usecase.local.UpdateWordLevelUseCase
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.common.utils.succeededRemotely
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WordService @Inject constructor(
    private val observeWordsByDictionaryUseCase: ObserveWordsByDictionaryUseCase,
    private val observeWordsInLanguagePairUseCase: ObserveWordsInLanguagePairUseCase,
    private val observeWordCountsUseCase: ObserveWordCountsUseCase,
    private val deleteWordApiUseCase: DeleteWordApiUseCase,
    private val deleteWordUseCase: DeleteWordUseCase,
    private val markWordDeletedUseCase: MarkWordDeletedUseCase,
    private val getWordUseCase: GetWordUseCase,
    private val saveWordUseCase: SaveWordUseCase,
    private val updateWordLevelUseCase: UpdateWordLevelUseCase,
    private val observeDictionaryUseCase: ObserveDictionaryUseCase,
    private val importedDictionaryService: ImportedDictionaryService,
) {

    /**
     * A dictionary's words — own or downloaded. A dictionary id lives in exactly one of the two
     * stores, so the other side is simply empty.
     */
    fun observeWordsByDictionary(dictionaryId: String): Flow<List<WordUi>> =
        combine(
            observeWordsByDictionaryUseCase.invoke(dictionaryId).map { it.map(Word::convertToUi) },
            importedDictionaryService.observeWords(dictionaryId),
        ) { own, imported -> own + imported }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    /**
     * Words from every dictionary — own and downloaded — sharing this one's language direction
     * (its own included). Backs the test's distractor pool and the "enough words to build a test"
     * check.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeWordsInLanguagePair(dictionaryId: String): Flow<List<WordUi>> =
        combine(
            observeDictionaryUseCase.invoke(dictionaryId).map { it?.let { d -> d.fromLang to d.toLang } },
            importedDictionaryService.observeDictionary(dictionaryId).map { it?.let { d -> d.fromLang to d.toLang } },
        ) { own, imported -> own ?: imported }
            .distinctUntilChanged()
            .flatMapLatest { languages ->
                if (languages == null) {
                    flowOf(emptyList())
                } else {
                    val (fromLang, toLang) = languages
                    combine(
                        observeWordsInLanguagePairUseCase.invoke(fromLang, toLang).map { it.map(Word::convertToUi) },
                        importedDictionaryService.observeWordsInLanguagePair(fromLang, toLang),
                    ) { own, imported -> own + imported }
                }
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    /** Word count per dictionary id — own and downloaded — for the dictionary list. */
    fun observeWordCounts(): Flow<Map<String, Int>> =
        combine(
            observeWordCountsUseCase.invoke(),
            importedDictionaryService.observeWordCounts(),
        ) { own, imported -> own + imported }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    suspend fun getWord(wordId: String): WordUi =
        getWordUseCase.invoke(wordId).convertToUi()

    suspend fun saveWord(word: Word){
        saveWordUseCase.invoke(word)
    }

    /**
     * Saves practice progress without rewriting the rest of the word. Practice screens work from a
     * snapshot that can go stale mid-session, so only the level column is touched — a word deleted
     * while the session was open stays deleted.
     */
    suspend fun updateWordLevel(wordId: String, level: Int) {
        updateWordLevelUseCase.invoke(
            wordId = wordId,
            level = level,
            updatedAt = getNowInMillis(),
        )
        // A downloaded word lives in the other table; its progress stays on this device. The id is
        // in only one of the two, so exactly one of these updates matches a row.
        importedDictionaryService.updateWordLevel(wordId, level)
    }

    /**
     * Tombstones the word first so the deletion is immediate and offline-safe, then hard-deletes
     * once the server confirms. A failed or offline API call leaves the tombstone in place;
     * the sync upload phase retries it.
     */
    suspend fun deleteWord(wordId: String) {
        markWordDeletedUseCase.invoke(wordId)
        if (succeededRemotely { deleteWordApiUseCase.invoke(wordId) }) {
            deleteWordUseCase.invoke(wordId)
        }
    }


}
