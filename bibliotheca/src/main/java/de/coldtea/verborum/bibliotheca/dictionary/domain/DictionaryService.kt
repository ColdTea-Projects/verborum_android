package de.coldtea.verborum.bibliotheca.dictionary.domain

import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.data.db.entity.DictionaryEntity.Companion.GUEST_USER_ID
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.Dictionary
import de.coldtea.verborum.bibliotheca.dictionary.domain.model.DictionaryDeletion
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.api.DeleteDictionaryRemotelyUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.GetDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.MarkDictionaryDeletedUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveAllDictionariesUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.ObserveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.domain.usecase.local.SaveDictionaryUseCase
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.imported.domain.ImportedDictionaryService
import de.coldtea.verborum.core.auth.domain.usecase.GetActiveUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DictionaryService @Inject constructor(
    private val observeAllDictionariesUseCase: ObserveAllDictionariesUseCase,
    private val observeDictionaryUseCase: ObserveDictionaryUseCase,
    private val getDictionaryUseCase: GetDictionaryUseCase,
    private val saveDictionaryUseCase: SaveDictionaryUseCase,
    private val markDictionaryDeletedUseCase: MarkDictionaryDeletedUseCase,
    private val deleteDictionaryRemotelyUseCase: DeleteDictionaryRemotelyUseCase,
    private val getActiveUserUseCase: GetActiveUserUseCase,
    private val importedDictionaryService: ImportedDictionaryService,
) {

    /**
     * Deduplicated on the *UI* model on purpose: sync flips fields the list never renders
     * (`isSynced`, and tags — shown only on the detail screen). Comparing after the mapping means
     * those writes produce no emission at all, so the list does not recompose for changes it
     * cannot show. Tags are dropped from the projection so a tag-only edit can't re-emit the list.
     */
    fun observeDictionaries(): Flow<List<DictionaryUi>> = combine(
        observeAllDictionariesUseCase
            .invoke()
            .map { it.map { dictionary -> dictionary.convertToUi().copy(tags = emptyList()) } },
        // Downloaded dictionaries sit beside the user's own, marked isImported.
        importedDictionaryService.observeDictionaries()
            .map { it.map { dictionary -> dictionary.copy(tags = emptyList()) } },
    ) { own, imported -> own + imported }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    /**
     * Own or downloaded. Emits null once the dictionary is tombstoned, removed or unsubscribed,
     * letting screens react to it going away.
     */
    fun observeDictionary(dictionaryId: String): Flow<DictionaryUi?> = combine(
        observeDictionaryUseCase.invoke(dictionaryId).distinctUntilChanged().map { it?.convertToUi() },
        importedDictionaryService.observeDictionary(dictionaryId),
    ) { own, imported -> own ?: imported }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)

    /**
     * Stops following a downloaded dictionary: removed from the vault, then from this device.
     * Online-only — throws when the server cannot be reached, and nothing changes locally then.
     */
    suspend fun unsubscribe(dictionaryId: String) = importedDictionaryService.unsubscribe(dictionaryId)

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

    suspend fun createDictionary(
        name: String,
        fromLang: String,
        toLang: String,
        tags: List<String> = emptyList(),
    ): String {
        val dictionary =
            Dictionary(
                dictionaryId = "",
                // Stamp the signed-in owner; the guest UUID only survives as an offline fallback.
                userId = getActiveUserUseCase.invoke() ?: GUEST_USER_ID,
                name = name,
                isPublic = false,
                isSynced = false,
                fromLang = fromLang,
                toLang = toLang,
                createdAt = getNowInMillis(),
                updatedAt = getNowInMillis(),
                tags = tags,
            )

        return saveDictionaryUseCase.invoke(dictionary)
    }

    /**
     * Deletes one of the user's dictionaries. The server is asked first, because it may refuse
     * (a member's last shared dictionary, marketplace guide §4.6) — then nothing changes and the
     * caller tells the user why. Only when the server cannot be reached is the dictionary
     * tombstoned: hidden at once, and the sync upload phase retries the delete.
     */
    suspend fun deleteDictionary(dictionaryId: String): DictionaryDeletion {
        val result = deleteDictionaryRemotelyUseCase.invoke(dictionaryId)
        if (result == DictionaryDeletion.PENDING) markDictionaryDeletedUseCase.invoke(dictionaryId)
        return result
    }
}