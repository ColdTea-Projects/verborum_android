package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.data.api.DictionaryApi
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.GetAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.domain.SaveWordUseCase
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

class SyncUserDictionariesUseCase @Inject constructor(
    private val dictionaryApi: DictionaryApi,
    private val wordApi: WordApi,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val saveWordUseCase: SaveWordUseCase,
    private val removeAllDictionariesUseCase: GetAllDictionariesUseCase,
    //TODO: getActiveUserUseCase
) {

    suspend fun invoke() {
        val activeUser = GUEST_USER_ID // TODO: getActiveUserUseCase.invoke()
        removeAllDictionariesUseCase.invoke() // TODO: make it update time base
        dictionaryApi.getAllDictionariesByUser(activeUser)?.map { dictResponse ->
            val words = wordApi.getWordsByDictionary(dictResponse.dictionaryId.orEmpty())?.map { wordResponse ->
                wordResponse.convertToWord(dictResponse.dictionaryId.orEmpty())
            }
            save(dictResponse.convertToDictionary(), words)
        }
    }

    private suspend fun save(dictionary: Dictionary, word: List<Word>?){
        saveDictionaryUseCase.invoke(dictionary)
        word?.map { saveWordUseCase.invoke(it) }
    }
}