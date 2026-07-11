package de.coldtea.verborum.bibliotheca.word.domain.usecase.api

import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import javax.inject.Inject

class DeleteWordApiUseCase @Inject constructor(
    private val wordApi: WordApi,
) {
    suspend fun invoke(wordId: String) = wordApi.deleteWord(wordId)
}
