package de.coldtea.verborum.bibliotheca.imported.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.utils.getNowInMillis
import de.coldtea.verborum.bibliotheca.dictionary.ui.dictionarylist.model.DictionaryUi
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedDictionary
import de.coldtea.verborum.bibliotheca.imported.domain.model.ImportedWord
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.FetchImportedDictionaryApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.GetVaultApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.api.RemoveFromVaultApiUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.DeleteImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.GetImportedDictionariesUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionariesUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionaryIdsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordCountsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordsInLanguagePairUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.ObserveImportedWordsUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.SaveImportedDictionaryUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.usecase.local.UpdateImportedWordLevelUseCase
import de.coldtea.verborum.bibliotheca.word.ui.model.WordUi
import de.coldtea.verborum.core.profile.domain.ProfileService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The dictionaries the user downloaded from the Forum: local, read-only copies of other users'
 * public dictionaries, mirrored from the vault (`docs/marketplace-client-guide.md` §6.6–§6.9).
 *
 * Kept apart from the user's own dictionaries end to end — own tables, no upload path — so the
 * sync engine can never push them (every write to someone else's dictionary is a 403). The only
 * local change is the user's practice progress on their words.
 */
class ImportedDictionaryService @Inject constructor(
    private val observeImportedDictionariesUseCase: ObserveImportedDictionariesUseCase,
    private val observeImportedDictionaryUseCase: ObserveImportedDictionaryUseCase,
    private val observeImportedDictionaryIdsUseCase: ObserveImportedDictionaryIdsUseCase,
    private val observeImportedWordsUseCase: ObserveImportedWordsUseCase,
    private val observeImportedWordsInLanguagePairUseCase: ObserveImportedWordsInLanguagePairUseCase,
    private val observeImportedWordCountsUseCase: ObserveImportedWordCountsUseCase,
    private val updateImportedWordLevelUseCase: UpdateImportedWordLevelUseCase,
    private val getImportedDictionariesUseCase: GetImportedDictionariesUseCase,
    private val saveImportedDictionaryUseCase: SaveImportedDictionaryUseCase,
    private val deleteImportedDictionaryUseCase: DeleteImportedDictionaryUseCase,
    private val fetchImportedDictionaryApiUseCase: FetchImportedDictionaryApiUseCase,
    private val getVaultApiUseCase: GetVaultApiUseCase,
    private val removeFromVaultApiUseCase: RemoveFromVaultApiUseCase,
    private val profileService: ProfileService,
) {
    fun observeDictionaries(): Flow<List<DictionaryUi>> =
        observeImportedDictionariesUseCase.invoke()
            .map { it.map(ImportedDictionary::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    fun observeDictionary(dictionaryId: String): Flow<DictionaryUi?> =
        observeImportedDictionaryUseCase.invoke(dictionaryId)
            .map { it?.convertToUi() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    fun observeDictionaryIds(): Flow<Set<String>> =
        observeImportedDictionaryIdsUseCase.invoke()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    fun observeWords(dictionaryId: String): Flow<List<WordUi>> =
        observeImportedWordsUseCase.invoke(dictionaryId)
            .map { it.map(ImportedWord::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    fun observeWordsInLanguagePair(fromLang: String, toLang: String): Flow<List<WordUi>> =
        observeImportedWordsInLanguagePairUseCase.invoke(fromLang, toLang)
            .map { it.map(ImportedWord::convertToUi) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    fun observeWordCounts(): Flow<Map<String, Int>> =
        observeImportedWordCountsUseCase.invoke()
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    /** No-op for a word that is not a downloaded one. */
    suspend fun updateWordLevel(wordId: String, level: Int) = withContext(Dispatchers.IO) {
        updateImportedWordLevelUseCase.invoke(wordId, level)
    }

    /** Copies a just-imported dictionary locally (guide §6.6). Throws when it cannot be read. */
    suspend fun addImportedDictionary(dictionaryId: String, publisherName: String?) = withContext(Dispatchers.IO) {
        val content = fetchImportedDictionaryApiUseCase.invoke(
            dictionaryId = dictionaryId,
            publisherName = publisherName,
            importedAt = getNowInMillis(),
        ) ?: return@withContext
        saveImportedDictionaryUseCase.invoke(content)
    }

    /**
     * Mirrors the vault: every downloaded dictionary is re-read in its owner's current version
     * (renames, new words), one no longer readable is dropped (guide §6.9), and one unsubscribed on
     * another device is removed — unless it was downloaded here moments ago, since the backend
     * adds the vault entry asynchronously after the import (guide §5.3).
     *
     * A dictionary that fails to load for any other reason keeps its local copy.
     */
    suspend fun refreshFromVault() = withContext(Dispatchers.IO) {
        val vault = getVaultApiUseCase.invoke(profileService.getUserId())
        val local = getImportedDictionariesUseCase.invoke().associateBy { it.dictionaryId }
        val vaultIds = vault.map { it.dictionaryId }.toSet()
        val now = getNowInMillis()

        local.values
            .filter { it.dictionaryId !in vaultIds && now - it.importedAt > VAULT_PROPAGATION_GRACE_MILLIS }
            .forEach { deleteImportedDictionaryUseCase.invoke(it.dictionaryId) }

        vault.forEach { entry ->
            val existing = local[entry.dictionaryId]
            try {
                val content = fetchImportedDictionaryApiUseCase.invoke(
                    dictionaryId = entry.dictionaryId,
                    publisherName = existing?.publisherName,
                    importedAt = existing?.importedAt ?: entry.importedAt ?: now,
                )
                if (content == null) {
                    deleteImportedDictionaryUseCase.invoke(entry.dictionaryId)
                } else {
                    saveImportedDictionaryUseCase.invoke(content)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("Imported refresh", e.message ?: "")
            }
        }
    }

    /**
     * Removes a downloaded dictionary: from the vault first, then locally — so it is only gone here
     * once the server agrees (online-only, like every vault change). Throws when the server cannot
     * be reached.
     */
    suspend fun unsubscribe(dictionaryId: String) = withContext(Dispatchers.IO) {
        removeFromVaultApiUseCase.invoke(userId = profileService.getUserId(), dictionaryId = dictionaryId)
        deleteImportedDictionaryUseCase.invoke(dictionaryId)
    }

    private companion object {
        const val VAULT_PROPAGATION_GRACE_MILLIS = 10 * 60 * 1000L
    }
}
