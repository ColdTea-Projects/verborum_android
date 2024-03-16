package de.coldtea.verborum.app.word.domain

import de.coldtea.verborum.app.common.utils.generateUUIDV4
import de.coldtea.verborum.app.word.data.WordRepository
import de.coldtea.verborum.app.word.domain.model.Word
import javax.inject.Inject

class SaveWordUseCase @Inject constructor(
    private val wordRepository: WordRepository,
) {
    suspend fun invoke(word: Word) =
        if (word.wordId.isBlank()) {
            createNewWord(word)
        } else {
            updateWord(word)
        }

    private suspend fun createNewWord(word: Word){
        val entity = word.copy(wordId = generateUUIDV4()).convertToEntity()
        wordRepository.saveWord(entity)
    }

    private suspend fun updateWord(word: Word){
        val entity = word.convertToEntity()
        wordRepository.saveWord(entity)
    }
}