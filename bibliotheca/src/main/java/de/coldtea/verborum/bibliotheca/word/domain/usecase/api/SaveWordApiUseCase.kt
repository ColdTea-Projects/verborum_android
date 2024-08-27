package de.coldtea.verborum.bibliotheca.word.domain.usecase.api

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordBundleRequest
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

class SaveWordApiUseCase @Inject constructor(
    private val wordApi: WordApi
) {
    suspend fun invoke(word: Word){

        if(word.wordId.isBlank()){
            val wordRequest = WordBundleRequest(
                dictionaryId = word.dictionaryId,
                words = listOf(word.convertToWordRequest().copy(wordId = generateUUIDV4()))
            )
            wordApi.createWords(listOf(wordRequest))
        }else {
            val wordRequest = WordBundleRequest(
                dictionaryId = word.dictionaryId,
                words = listOf(word.convertToWordRequest())
            )
            wordApi.updateWords(listOf(wordRequest))
        }
    }
}