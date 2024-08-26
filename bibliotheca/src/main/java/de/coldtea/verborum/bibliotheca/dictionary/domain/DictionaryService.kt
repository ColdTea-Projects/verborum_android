package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local.CleanDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local.GetDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecases.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DictionaryService @Inject constructor(
    private val observeAllDictionariesUseCase: ObserveAllDictionariesUseCase,
    private val getDictionaryUseCase: GetDictionaryUseCase,
    private val syncService: SyncService,
    private val uploadService: UploadService,
) {

    fun observeDictionaries(): Flow<List<DictionaryUi>> = observeAllDictionariesUseCase
        .invoke()
        .map { it.map { it.convertToUi() } }
        .flowOn(Dispatchers.IO)

    suspend fun getDictionary(dictionaryId: String): DictionaryUi = getDictionaryUseCase
        .invoke(dictionaryId)
        .convertToUi()

    suspend fun crateDummyDictionary() {
        uploadService.createDictionary(
            Dictionary(
                dictionaryId = "",
                userId = GUEST_USER_ID,
                name = generateRandomString(),
                isPublic = false,
                fromLang = "DE",
                toLang = "EN",
                createdAt = getNowInMillis(),
                updatedAt = getNowInMillis(),
            )
        )

        syncService.syncDictionaries()
    }

    suspend fun deleteDictionary(dictionaryId: String) = uploadService.deleteDictionary(dictionaryId)

    fun generateRandomString(len: Int = 15): String {
        val alphanumerics = CharArray(26) { it -> (it + 97).toChar() }.toSet()
            .union(CharArray(9) { it -> (it + 48).toChar() }.toSet())
        return (0..len - 1).map {
            alphanumerics.toList().random()
        }.joinToString("")
    }
}