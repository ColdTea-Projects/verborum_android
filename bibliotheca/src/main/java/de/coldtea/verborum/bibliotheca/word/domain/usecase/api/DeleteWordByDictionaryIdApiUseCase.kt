package de.coldtea.verborum.bibliotheca.word.domain.usecase.api

import de.coldtea.verborum.bibliotheca.common.utils.generateUUIDV4
import de.coldtea.verborum.bibliotheca.word.data.api.WordApi
import de.coldtea.verborum.bibliotheca.word.data.api.model.WordBundleRequest
import de.coldtea.verborum.bibliotheca.word.domain.model.Word
import javax.inject.Inject

class DeleteWordByDictionaryIdApiUseCase @Inject constructor(
    private val wordApi: WordApi
) {
    suspend fun invoke(dictionaryId: String) =
        wordApi.deleteWordsByDictionaryId(dictionaryId)
}