package de.coldtea.verborum.bibliotheca.common.domain

import android.util.Log
import de.coldtea.verborum.bibliotheca.common.domain.usecases.SyncUserDictionariesUseCase
import de.coldtea.verborum.bibliotheca.common.domain.usecases.UploadPendingChangesUseCase
import de.coldtea.verborum.bibliotheca.imported.domain.ImportedDictionaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncService @Inject constructor(
    private val uploadPendingChangesUseCase: UploadPendingChangesUseCase,
    private val syncUserDictionariesUseCase: SyncUserDictionariesUseCase,
    private val importedDictionaryService: ImportedDictionaryService,
) {

    suspend fun syncDictionaries() = withContext(Dispatchers.IO) {
        // Upload local changes first so the download merge cannot drop data the server
        // has never seen. Each phase fails independently.
        uploadPendingChanges()
        try {
            syncUserDictionariesUseCase.invoke()
        } catch (ex: Exception) {
            Log.e("Sync error", ex.message ?: "")
        }
        // Downloaded dictionaries are only ever read: mirror the vault, never upload.
        try {
            importedDictionaryService.refreshFromVault()
        } catch (ex: Exception) {
            Log.e("Imported sync error", ex.message ?: "")
        }
    }

    /**
     * Just the upload half — push local changes to the server without pulling the whole dataset
     * back. Used by the immediate, change-triggered sync so getting data *up* stays cheap; the
     * download-merge is left to the periodic reconcile and screen-open sync.
     */
    suspend fun uploadPendingChanges() = withContext(Dispatchers.IO) {
        try {
            uploadPendingChangesUseCase.invoke()
        } catch (ex: Exception) {
            Log.e("Upload error", ex.message ?: "")
        }
    }
}
