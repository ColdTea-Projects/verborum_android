package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetAllDictionariesUseCase
import de.coldtea.verborum.core.library.domain.OwnedDictionariesSource
import de.coldtea.verborum.core.library.domain.model.DictionaryLanguages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Bibliotheca's dictionaries for other features. The local database only ever holds the signed-in
 * user's rows (it is wiped on logout), so every non-deleted one is theirs.
 */
class BibliothecaOwnedDictionariesSource @Inject constructor(
    private val getAllDictionariesUseCase: GetAllDictionariesUseCase,
) : OwnedDictionariesSource {

    override suspend fun getOwnedDictionaryLanguages(): List<DictionaryLanguages> =
        withContext(Dispatchers.IO) {
            getAllDictionariesUseCase.invoke()
                .filterNot { it.isDeleted }
                .map { dictionary ->
                    DictionaryLanguages(
                        fromLang = dictionary.fromLang,
                        toLang = dictionary.toLang,
                        // updatedAt is 0 until the server first stamps it.
                        lastActivityAt = maxOf(dictionary.updatedAt, dictionary.createdAt),
                    )
                }
        }
}
