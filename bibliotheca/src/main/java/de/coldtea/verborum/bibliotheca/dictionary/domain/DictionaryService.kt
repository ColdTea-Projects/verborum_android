package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.domain.SyncService
import de.coldtea.verborum.bibliotheca.common.domain.UploadService
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.DeleteDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionaryDeletedUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.model.DictionaryUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.collections.map

class DictionaryService @Inject constructor(
    private val observeAllDictionariesUseCase: ObserveAllDictionariesUseCase,
    private val observeDictionaryUseCase: ObserveDictionaryUseCase,
    private val getDictionaryUseCase: GetDictionaryUseCase,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val deleteDictionaryUseCase: DeleteDictionaryUseCase,
    private val markDictionaryDeletedUseCase: MarkDictionaryDeletedUseCase,
    private val syncService: SyncService,
    private val uploadService: UploadService,
) {

    fun observeDictionaries(): Flow<List<DictionaryUi>> = observeAllDictionariesUseCase
        .invoke()
        .distinctUntilChanged()
        .map { it.map(Dictionary::convertToUi) }
        .flowOn(Dispatchers.IO)

    /** Emits null once the dictionary is tombstoned or removed, letting screens react to deletion. */
    fun observeDictionary(dictionaryId: String): Flow<DictionaryUi?> = observeDictionaryUseCase
        .invoke(dictionaryId)
        .distinctUntilChanged()
        .map { it?.convertToUi() }
        .flowOn(Dispatchers.IO)

    /** One-shot read for prefilling the edit screen. */
    suspend fun getDictionary(dictionaryId: String): DictionaryUi =
        getDictionaryUseCase.invoke(dictionaryId).convertToUi()

    /** Saves edits to an existing dictionary, preserving id/owner/creation and re-marking unsynced. */
    suspend fun updateDictionary(dictionary: DictionaryUi): String {
        val updated = dictionary.convertToDictionary().copy(
            updatedAt = getNowInMillis(),
            isSynced = false,
        )
        return saveDictionaryUseCase.invoke(updated)
    }

    suspend fun createDictionary(name: String, fromLang: String, toLang: String): String {
        val dictionary =
            Dictionary(
                dictionaryId = "",
                userId = GUEST_USER_ID,
                name = name,
                isPublic = false,
                isSynced = false,
                fromLang = fromLang,
                toLang = toLang,
                createdAt = getNowInMillis(),
                updatedAt = getNowInMillis(),
            )

        return saveDictionaryUseCase.invoke(dictionary)
    }

    /** Tombstones the dictionary: hidden locally at once, offline-safe. */
    suspend fun markDictionaryDeleted(dictionaryId: String) =
        markDictionaryDeletedUseCase.invoke(dictionaryId)

    /** Hard-deletes locally only when the server confirms; otherwise the tombstone remains. */
    suspend fun deleteDictionary(dictionaryId: String) {
        if (uploadService.deleteDictionary(dictionaryId).isSuccessful) {
            deleteDictionaryUseCase.invoke(dictionaryId)
        }
    }
}