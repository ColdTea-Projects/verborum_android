package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUserCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUserCase
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import javax.inject.Inject

class UploadService @Inject constructor(
    private val saveDictionaryApiUserCase: SaveDictionaryApiUserCase,
    private val deleteDictionaryApiUserCase: DeleteDictionaryApiUserCase,
    private val saveWordApiUseCase: SaveWordApiUseCase,
) {

    suspend fun createDictionary(dictionary: Dictionary) = saveDictionaryApiUserCase
        .invoke(dictionary)

    suspend fun deleteDictionary(dictionaryId: String) = deleteDictionaryApiUserCase
        .invoke(dictionaryId)

    suspend fun createWord(word: Word) = saveWordApiUseCase
        .invoke(word)
}