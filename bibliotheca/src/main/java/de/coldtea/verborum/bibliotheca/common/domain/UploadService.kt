package de.coldtea.verborum.bibliotheca.common.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.SaveDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import de.coldtea.verborum.bibliotheca.word.domain.usecase.api.SaveWordApiUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UploadService @Inject constructor(
    private val saveDictionaryApiUseCase: SaveDictionaryApiUseCase,
    private val deleteDictionaryApiUseCase: DeleteDictionaryApiUseCase,
    private val saveWordApiUseCase: SaveWordApiUseCase,
) {

    suspend fun createDictionary(dictionary: Dictionary) = withContext(Dispatchers.IO) {
        saveDictionaryApiUseCase.invoke(dictionary)
    }

    suspend fun deleteDictionary(dictionaryId: String) = withContext(Dispatchers.IO) {
        deleteDictionaryApiUseCase.invoke(dictionaryId)
    }

    suspend fun createWord(word: Word) = withContext(Dispatchers.IO) {
        saveWordApiUseCase.invoke(word)
    }
}